(ns rems.context
  "Collection of the global variables for REMS.

   When referring, please make your use greppable with the prefix context,
   i.e. context/*root-path*."
  (:require [cprop.core :refer [load-config]]
            [cprop.source :refer [from-file]]))

(defn load-default-theme []
  (from-file "resources/themes/default.edn"))

(defn load-theme
  "Tries to load the default theme and override default values by merging them with the values from the theme given as a parameter."
  ([theme]
     (if theme
       (try
         (merge (load-default-theme)
                (from-file (str "resources/themes/" theme ".edn")))
         (catch java.io.FileNotFoundException e
           (load-default-theme)))
       (load-default-theme)))
  ([]
   (load-theme (:theme (load-config)))))

(def ^:dynamic ^{:doc "Application root path also known as context-path.

  If application does not live at '/',
  then this is the path before application relative paths."}
  *root-path*)

(def ^:dynamic ^{:doc "User data available from request."} *user*)

(def ^:dynamic ^{:doc "Active role for user or nil"} *active-role*)

(def ^:dynamic ^{:doc "Set of roles for user (or nil)"} *roles*)

(def ^:dynamic ^{:doc "Tempura object initialized with user's preferred language."}
  *tempura*)

(def ^:dynamic ^{:doc "User's preferred language."} *lang*)

(def ^:dynamic ^{:doc "Contents of the cart."} *cart*)

(def ^:dynamic ^{:doc "Flash session."} *flash*)

(def ^:dynamic ^{:doc "Theme related stylings for the site."} *theme* (load-theme))
