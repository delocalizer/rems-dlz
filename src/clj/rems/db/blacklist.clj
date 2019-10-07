(ns rems.db.blacklist
  (:require [clj-time.format :as time-format]
            [rems.db.core :as db]
            [rems.json :as json]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [schema.utils] )
  (:import (org.joda.time DateTime)))

;; TODO copied from rems.application.events:
(def UserId s/Str)

(def BlacklistEvent
  {(s/optional-key :event/id) s/Int
   :event/type (s/enum :blacklist.event/add :blacklist.event/remove)
   :event/time DateTime
   :event/actor UserId
   :blacklist/user s/Str
   :blacklist/resource s/Str ;; i.e. resource/ext-id
   :event/comment (s/maybe s/Str)})

;; TODO copied from rems.db.events:
(defn- datestring->datetime [s]
  (if (string? s)
    (time-format/parse s)
    s))
(def ^:private datestring-coercion-matcher
  {DateTime datestring->datetime})
(defn- coercion-matcher [schema]
  (or (datestring-coercion-matcher schema)
      (coerce/string-coercion-matcher schema)))


(def ^:private coerce-event
  (coerce/coercer BlacklistEvent coercion-matcher))

;; TODO factor out some utils from here and rems.db.events?
(defn- json->event [json]
  (let [result (-> json
                   json/parse-string
                   coerce-event)]
    (when (schema.utils/error? result)
      (throw (ex-info (str "Value does not match schema: " (pr-str result))
                      {:value json :error result})))
    result))

(defn- event->json [event]
  (s/validate BlacklistEvent event)
  (json/generate-string event))

(defn add-event! [event]
  (db/add-blacklist-event! {:eventdata (event->json event)}))

(defn- event-from-db [event]
  (assoc (json->event (:eventdata event))
         :event/id (:id event))) ;; TODO :event/id ranges conflict between blacklist_event and application_event

(defn get-events [params]
  (mapv event-from-db (db/get-blacklist-events {:user (:blacklist/user params)
                                                :resource (:blacklist/resource params)})))
