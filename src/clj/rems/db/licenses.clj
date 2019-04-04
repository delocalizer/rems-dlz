(ns rems.db.licenses
  "querying localized licenses"
  (:require [rems.common-util :refer [distinct-by]]
            [rems.db.core :as db]
            [rems.db.resource :as resource]
            [rems.db.workflow :as workflow])
  (:import (java.io FileInputStream ByteArrayOutputStream)))

(defn- format-license [license]
  {:id (:id license)
   :licensetype (:type license)
   :start (:start license)
   :end (:endt license)
   :enabled (:enabled license)
   :archived (:archived license)
   :active (:active license)
   ;; TODO why do licenses have a non-localized title & content while items don't?
   :title (:title license)
   :textcontent (:textcontent license)
   :attachment-id (:attachmentid license)})

(defn- format-licenses [licenses]
  (mapv format-license licenses))

(defn- get-license-localizations []
  (->> (db/get-license-localizations)
       (map #(update-in % [:langcode] keyword))
       (group-by :licid)))

(defn- localize-license [localizations license]
  (assoc license :localizations
         (into {} (for [{:keys [langcode title textcontent attachmentid]} (get localizations (:id license))]
                    [langcode {:title title
                               :textcontent textcontent
                               :attachment-id attachmentid}]))))

(defn- localize-licenses [licenses]
  (mapv (partial localize-license (get-license-localizations)) licenses))

(defn get-resource-licenses
  "Get resource licenses for given resource id"
  [id]
  (->> (db/get-resource-licenses {:id id})
       (format-licenses)
       (mapv db/assoc-active)
       (localize-licenses)))

(defn get-license
  "Get a single license by id"
  [id]
  (->> (db/get-license {:id id})
       (format-license)
       (db/assoc-active)
       (localize-license (get-license-localizations))))

;; NB! There are three different "license activity" concepts:
;; - start and end in resource_licenses table
;; - start and end in workflow_licenses table
;; - start and end in licenses table
;;
;; The last of these is only used in get-all-licenses which is only
;; used by /api/licenses. The resource and workflow activities are
;; used in actual application processing logic.

(defn get-all-licenses
  "Get all licenses.

   filters is a map of key-value pairs that must be present in the licenses"
  [filters]
  (->> (db/get-all-licenses)
       (map db/assoc-active)
       (db/apply-filters filters)
       (format-licenses)
       (localize-licenses)))

(defn get-active-licenses
  "Get license active now. Params map can contain:
    :wfid -- workflow to get workflow licenses for
    :items -- sequence of catalogue items to get resource licenses for"
  [now params]
  (->> (db/get-licenses params)
       (format-licenses)
       (localize-licenses)
       (map (partial db/assoc-active now))
       (filter :active)
       (distinct-by :id)))

(defn create-license! [{:keys [title licensetype textcontent localizations attachment-id]} user-id]
  (let [license (db/create-license! {:owneruserid user-id
                                     :modifieruserid user-id
                                     :type licensetype
                                     :title title
                                     :textcontent textcontent
                                     :attachmentId attachment-id})
        licid (:id license)]
    (doseq [[langcode localization] localizations]
      (db/create-license-localization! {:licid licid
                                        :langcode (name langcode)
                                        :title (:title localization)
                                        :textcontent (:textcontent localization)
                                        :attachmentId (:attachment-id localization)}))
    {:success (not (nil? licid))
     :id licid}))

(defn create-license-attachment! [{:keys [tempfile filename content-type]} user-id]
  (let [byte-array (with-open [input (FileInputStream. tempfile)
                               buffer (ByteArrayOutputStream.)]
                     (clojure.java.io/copy input buffer)
                     (.toByteArray buffer))]
    (select-keys
     (db/create-license-attachment! {:user user-id
                                     :filename filename
                                     :type content-type
                                     :data byte-array})
     [:id])))

(defn remove-license-attachment!
  [attachment-id]
  (db/remove-license-attachment! {:id attachment-id}))

(defn- get-license-usage [id]
  ;; these could be db joins
  (let [resources (->> (db/get-resources-for-license {:id id})
                       (map :resid)
                       (map resource/get-resource)
                       (remove :archived)
                       (map #(select-keys % [:id :resid])))
        workflows (->> (db/get-workflows-for-license {:id id})
                       (map :wfid)
                       (map workflow/get-workflow)
                       (remove :archived)
                       (map #(select-keys % [:id :title])))]
    (when (or (seq resources) (seq workflows))
      {:resources resources
       :workflows workflows})))

(defn update-license! [command]
  (let [usage (get-license-usage (:id command))]
    (if (and (:archived command) usage)
      {:success false
       :errors [(merge {:type :t.administration.errors/license-in-use}
                       usage)]}
      (do
        (db/set-license-state! command)
        {:success true}))))
