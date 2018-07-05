(ns rems.api
  (:require [compojure.api.exception :as ex]
            [compojure.api.sweet :refer :all]
            [rems.api.actions :refer [actions-api]]
            [rems.api.applications :refer [applications-api]]
            [rems.api.catalogue :refer [catalogue-api]]
            [rems.api.catalogue-items :refer [catalogue-items-api]]
            [rems.api.entitlements :refer [entitlements-api]]
            [rems.api.forms :refer [forms-api]]
            [rems.api.licenses :refer [licenses-api]]
            [rems.api.public :as public]
            [rems.api.resources :refer [resources-api]]
            [rems.api.workflows :refer [workflows-api]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.http-response :refer :all]
            [schema.core :as s])
  (:import rems.auth.NotAuthorizedException))

(defn unauthorized-handler
  [exception ex-data request]
  (unauthorized "unauthorized"))

(defn invalid-handler
  [exception ex-data request]
  (bad-request (.getMessage exception)))

(def cors-middleware
  #(wrap-cors
    %
    :access-control-allow-origin #".*"
    :access-control-allow-methods [:get :put :post :delete]))

(def api-routes
  (api
   {;; TODO: should this be in rems.middleware?
    :middleware [cors-middleware]
    :exceptions {:handlers {rems.auth.NotAuthorizedException (ex/with-logging unauthorized-handler)
                            rems.InvalidRequestException (ex/with-logging invalid-handler)
                            ;; add logging to validation handlers
                            ::ex/request-validation (ex/with-logging ex/request-validation-handler)
                            ::ex/request-parsing (ex/with-logging ex/request-parsing-handler)
                            ::ex/response-validation (ex/with-logging ex/response-validation-handler)}}
    :swagger {:ui "/swagger-ui"
              :spec "/swagger.json"
              :data {:info {:version "1.0.0"
                            :title "REMS API"
                            :description "REMS API Services"}}}}

   (context "/api" []
     :header-params [{x-rems-api-key :- (describe s/Str "REMS API-Key (optional for UI, required for API)") nil}
                     {x-rems-user-id :- (describe s/Str "user id (optional for UI, required for API)") nil}]

     public/translations-api
     public/theme-api
     public/config-api

     actions-api
     applications-api
     catalogue-api
     catalogue-items-api
     entitlements-api
     forms-api
     licenses-api
     resources-api
     workflows-api)))
