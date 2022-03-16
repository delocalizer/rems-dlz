(ns rems.auth.oidc
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [medley.core :refer [find-first]]
            [rems.config :refer [env oidc-configuration]]
            [rems.db.user-mappings :as user-mappings]
            [rems.db.users :as users]
            [rems.ga4gh :as ga4gh]
            [rems.json :as json]
            [rems.jwt :as jwt]
            [rems.util :refer [getx]]
            [ring.util.response :refer [redirect]])
  (:import [java.time Instant]))

(defn login-url []
  (str (:authorization_endpoint oidc-configuration)
       "?response_type=code"
       "&client_id=" (getx env :oidc-client-id)
       "&redirect_uri=" (getx env :public-url) "oidc-callback"
       "&scope=" (getx env :oidc-scopes)
       (getx env :oidc-additional-authorization-parameters)
       #_"&state=STATE")) ; FIXME We could use the state for intelligent redirect. Also check if we need it for CSRF protection as Auth0 docs say.

(defn logout-url []
  "/oidc-logout")

(defn- get-userid-attributes [id-data]
  (for [{:keys [attribute rename]} (getx env :oidc-userid-attributes)
        :let [value (get id-data (keyword attribute))]
        :when value]
    [(keyword (or rename attribute)) value]))

(deftest test-get-userid-attributes
  (with-redefs [env {:oidc-userid-attributes [{:attribute "sub" :rename "elixirId"}
                                              {:attribute "old_sub"}]}]
    (is (= [] (get-userid-attributes nil)))
    (is (= [[:elixirId "elixir-alice"]
            [:old_sub "alice"]]
           (get-userid-attributes {:old_sub "alice"
                                   :sub "elixir-alice"
                                   :name "Alice Applicant"})))))

(defn- get-new-userid
  "Returns a new userid for a user based on the given `id-data` identity data.

  The userid will actually be the first valid attribute value from the identity data."
  [id-data]
  (first (keep second (get-userid-attributes id-data))))

(deftest test-get-new-userid
  (with-redefs [env {:oidc-userid-attributes [{:attribute "atr"}]}]
    (is (nil? (get-new-userid {:sub "123"})))
    (is (= "456" (get-new-userid {:sub "123" :atr "456"}))))

  (with-redefs [env {:oidc-userid-attributes [{:attribute "atr"}
                                              {:attribute "sub"}
                                              {:attribute "fallback"}]}]
    (is (= "123" (get-new-userid {:sub "123"})))
    (is (= "456" (get-new-userid {:sub "123" :atr "456"})))
    (is (= "456" (get-new-userid {:sub "123" :atr "456" :fallback "78"})))
    (is (= "78" (get-new-userid {:fallback "78"}))))

  (with-redefs [env {:oidc-userid-attributes [{:attribute "atr" :rename "elixirId"}
                                              {:attribute "sub"}]}]
    (is (= "elixir-alice" (get-new-userid {:atr "elixir-alice" :sub "123"})))
    (is (= "123" (get-new-userid {:elixirId "elixir-alice" :sub "123"}))
        "user data should use names before rename")))

(defn save-user-mappings! [id-data userid]
  (let [attrs (get-userid-attributes id-data)]
    (doseq [[attr value] attrs
            :when (not= value userid)]
      (user-mappings/create-user-mapping! {:userid userid
                                           :ext-id-attribute (name attr)
                                           :ext-id-value value}))))

(defn- find-user [id-data]
  (let [userid-attrs (get-userid-attributes id-data)
        user-mapping-match (fn [[attribute value]]
                             (let [mappings (user-mappings/get-user-mappings {:ext-id-attribute attribute :ext-id-value value})]
                               (:userid (first mappings))))] ; should be at most one by kv search
    (or (some user-mapping-match userid-attrs)
        (find-first users/user-exists? (map second userid-attrs)))))

(defn- upsert-user! [user]
  (let [userid (:eppn user)]
    (users/add-user-raw! userid user)
    user))

(defn- get-user-attributes [id-data user-info]
  ;; TODO all attributes could support :rename
  (let [userid (or (find-user id-data) (get-new-userid id-data))
        _ (assert userid)
        identity-base {:eppn userid
                       :commonName (some (comp id-data keyword) (:oidc-name-attributes env))
                       :mail (some (comp id-data keyword) (:oidc-email-attributes env))}
        extra-attributes (select-keys id-data (map (comp keyword :attribute) (:oidc-extra-attributes env)))
        user-info-attributes (select-keys user-info [:researcher-status-by])]
    (merge identity-base extra-attributes user-info-attributes)))

(defn find-or-create-user! [id-data user-info]
  (let [user (upsert-user! (get-user-attributes id-data user-info))]
    (save-user-mappings! id-data (:eppn user))
    user))

(defn oidc-callback [request]
  (let [response (-> (http/post (:token_endpoint oidc-configuration)
                                ;; NOTE Some IdPs don't support client id and secret in form params,
                                ;;      and require us to use HTTP basic auth
                                {:basic-auth [(str (getx env :oidc-client-id))
                                              (getx env :oidc-client-secret)]
                                 :form-params {:grant_type "authorization_code"
                                               :code (get-in request [:params :code])
                                               :redirect_uri (str (getx env :public-url) "oidc-callback")}
                                 ;; Setting these will cause the exceptions raised by http/post to contain
                                 ;; the request body, useful for debugging failures.
                                 :save-request? (getx env :log-authentication-details)
                                 :debug-body (getx env :log-authentication-details)})
                     ;; FIXME Complains about Invalid cookie header in logs
                     ;; TODO Unhandled responses for token endpoint:
                     ;;      403 {\"error\":\"invalid_grant\",\"error_description\":\"Invalid authorization code\"} when reusing codes
                     (:body)
                     (json/parse-string))
        access-token (:access_token response)
        id-token (:id_token response)
        issuer (:issuer oidc-configuration)
        audience (getx env :oidc-client-id)
        now (Instant/now)
        ;; id-data has keys:
        ;; sub – unique ID
        ;; name - non-unique name
        ;; locale – could be used to set preferred lang on first login
        ;; email – non-unique (!) email
        id-data (jwt/validate id-token issuer audience now)
        user-info (when-let [url (:userinfo_endpoint oidc-configuration)]
                    (-> (http/get url {:headers {"Authorization" (str "Bearer " access-token)}})
                        :body
                        json/parse-string
                        ga4gh/passport->researcher-status-by))
        user (find-or-create-user! id-data user-info)]
    (when (:log-authentication-details env)
      (log/info "logged in" id-data user-info user))
    (-> (redirect "/redirect")
        (assoc :session (:session request))
        (assoc-in [:session :access-token] access-token)
        (assoc-in [:session :identity] user))))

(defn- oidc-revoke [token]
  (when token
    (when-let [endpoint (:revocation_endpoint oidc-configuration)]
      (let [response (http/post endpoint
                                {:basic-auth [(getx env :oidc-client-id)
                                              (getx env :oidc-client-secret)]
                                 :form-params {:token token}
                                 :throw-exceptions false})]
        (when-not (= 200 (:status response))
          (log/error "received HTTP status" (:status response) "from" endpoint))))))

; TODO Logout. Federated or not?
; TODO Silent login when we have a new session, but user has logged in to auth provider

(defroutes routes
  (GET "/oidc-login" _req (redirect (login-url)))
  (GET "/oidc-logout" req
    (let [session (get req :session)]
      (when (:log-authentication-details env)
        (log/info "logging out" (:identity session)))
      (oidc-revoke (:access-token session))
      (assoc (redirect "/") :session (dissoc session :identity :access-token))))
  (GET "/oidc-callback" req (oidc-callback req)))

