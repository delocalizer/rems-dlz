(ns rems.css.styles
  "CSS stylesheets are generated by garden automatically when
  accessing the application on a browser. The garden styles can also
  be manually compiled by calling the function
  rems.css.styles/generate-css"
  (:require [garden.core :as g]
            [garden.selectors :as s]
            [garden.stylesheet :as stylesheet]
            [garden.units :as u]
            [garden.color :as c]
            [mount.core :refer [defstate]]
            [rems.util :as util]
            [clojure.string :as str]))

(defn- generate-at-font-faces []
  (list
   (stylesheet/at-font-face {:font-family "'Lato'"
                             :src "url('/font/Lato-Light.eot')"}
                            {:src "url('/font/Lato-Light.eot') format('embedded-opentype'), url('/font/Lato-Light.woff2') format('woff2'), url('/font/Lato-Light.woff') format('woff'), url('/font/Lato-Light.ttf') format('truetype')"
                             :font-weight 300
                             :font-style "normal"})
   (stylesheet/at-font-face {:font-family "'Lato'"
                             :src "url('/font/Lato-Regular.eot')"}
                            {:src "url('/font/Lato-Regular.eot') format('embedded-opentype'), url('/font/Lato-Regular.woff2') format('woff2'), url('/font/Lato-Regular.woff') format('woff'), url('/font/Lato-Regular.ttf') format('truetype')"
                             :font-weight 400
                             :font-style "normal"})
   (stylesheet/at-font-face {:font-family "'Lato'"
                             :src "url('/font/Lato-Bold.eot')"}
                            {:src "url('/font/Lato-Bold.eot') format('embedded-opentype'), url('/font/Lato-Bold.woff2') format('woff2'), url('/font/Lato-Bold.woff') format('woff'), url('/font/Lato-Bold.ttf') format('truetype')"
                             :font-weight 700
                             :font-style "normal"})))

(defn- generate-form-placeholder-styles []
  (list
   [".form-control::placeholder" {:color "#ccc"}] ; Standard
   [".form-control::-webkit-input-placeholder" {:color "#ccc"}] ; WebKit, Blink, Edge
   [".form-control:-moz-placeholder" {:color "#ccc"
                                      :opacity 1}] ; Mozilla Firefox 4 to 18
   [".form-control::-moz-placeholder" {:color "#ccc"
                                       :opacity 1}] ; Mozilla Firefox 19+
   [".form-control:-ms-input-placeholder" {:color "#ccc"}])) ; Internet Explorer 10-11

(defn resolve-image [path]
  (when path
    (let [url (if (str/starts-with? path "http")
                path
                (str (util/get-theme-attribute :img-path) path))]
      (str "url(\"" url "\")"))))

(defn- generate-media-queries []
  (list
   (stylesheet/at-media {:max-width (u/px 480)}
                        (list
                         [(s/descendant :.rems-table.cart :tr)
                          {:border-bottom "none"}]
                         [(s/descendant :.logo :.img)
                          {:background-color (util/get-theme-attribute :logo-bgcolor)
                           :background-image (resolve-image (util/get-theme-attribute :logo-name-sm))
                           :-webkit-background-size :contain
                           :-moz-background-size :contain
                           :-o-background-size :contain
                           :background-size :contain
                           :background-repeat :no-repeat
                           :background-position [[:center :center]]}]
                         [:.logo
                          {:height (u/px 150)}]))
   (stylesheet/at-media {:min-width (u/px 992)}
                        (list
                         [(s/descendant :.rems-table :td:before)
                          {:display "none"}]
                         [:.rems-table-search-toggle
                          {:display "flex !important"
                           :margin-top (u/px 20)}]
                         [:.rems-table
                          [:th
                           :td
                           {:display "table-cell"
                            :vertical-align "top"}]]
                         [:.rems-table
                          [:.column-header
                           {:white-space "nowrap"}]]
                         [:.rems-table
                          [:.column-filter
                           {:position "relative"}
                           [:input
                            {:width "100%"}]
                           [:.reset-button
                            {:position "absolute"
                             :right "0px"
                             :top "50%"
                             :margin-top "-0.5em"}]]] ; center vertically
                         [:.language-switcher
                          {:padding ".5em .5em"}]))
   (stylesheet/at-media {:min-width (u/px 480)}
                        [:.commands {:white-space "nowrap"}])))

(defn- generate-phase-styles []
  [:.phases {:width "100%"
             :height (u/px 40)
             :display "flex"
             :flex-direction "row"
             :justify-content "stretch"
             :align-items "center"}
   [:.phase {:background-color (util/get-theme-attribute :phase-bgcolor)
             :color (util/get-theme-attribute :phase-color)
             :flex-grow 1
             :height (u/px 40)
             :display "flex"
             :flex-direction "row"
             :justify-content "stretch"
             :align-items "center"}
    [:span {:flex-grow 1
            :text-align "center"
            :min-width (u/px 100)}]
    [(s/& ":not(:last-of-type):after") {:content "\"\""
                                        :border-top [[(u/px 20) :solid :white]]
                                        :border-left [[(u/px 10) :solid :transparent]]
                                        :border-bottom [[(u/px 20) :solid :white]]
                                        :border-right "none"}]
    [(s/& ":first-of-type") {:border-top-left-radius (u/px 4)
                             :border-bottom-left-radius (u/px 4)}]
    [(s/& ":last-of-type") {:border-top-right-radius (u/px 4)
                            :border-bottom-right-radius (u/px 4)}]
    [(s/& ":not(:first-of-type):before") {:content "\"\""
                                          :border-top [[(u/px 20) :solid :transparent]]
                                          :border-left [[(u/px 10) :solid :white]]
                                          :border-bottom [[(u/px 20) :solid :transparent]]
                                          :border-right "none"}]
    [:&.active (merge {:color (util/get-theme-attribute :phase-color-active :phase-color "#000")}
                      (if-let [background (util/get-theme-attribute :phase-background-active)]
                        {:background background}
                        {:background-color (util/get-theme-attribute :phase-bgcolor-active)
                         :border-color (util/get-theme-attribute
                                        :phase-bgcolor-active)}))]
    [:&.completed {:background-color (util/get-theme-attribute :phase-bgcolor-completed)
                   :border-color (util/get-theme-attribute :phase-bgcolor-completed)
                   :color (util/get-theme-attribute :phase-color-completed :phase-color "#fff")}]]])

(defn- generate-rems-table-styles []
  (list
   [:.rems-table.cart {:background "#fff"
                       :color "#000"
                       :margin 0}
    [:tr.separator {:border-bottom [[(u/px 1) :solid (util/get-theme-attribute :color1)]]}]
    [:td:before {:content "initial"}]
    [:th
     :td:before
     {:color "#000"}]
    [:tr
     [(s/& (s/nth-child "2n")) {:background "#fff"}]]]
   [:.rems-table-search-toggle ;; TODO: search fields are not visible in mobile mode
    {:display "none !important"}]
   [:#event-table
    {:white-space "pre-wrap"}
    [:.date {:min-width "160px"}]]
   [:.table-border {:padding 0
                    :margin "1em 0"
                    :border (util/get-theme-attribute :table-border "1px solid #ccc")
                    :border-radius (u/rem 0.4)}]
   [:.rems-table {:min-width "100%"
                  :background-color (util/get-theme-attribute :table-bgcolor :color1)
                  :box-shadow (util/get-theme-attribute :table-shadow)
                  :color (util/get-theme-attribute :table-text-color)
                  :border-radius (u/rem 0.4)
                  :overflow "hidden"}
    [:th {:color (util/get-theme-attribute :table-heading-color "#fff")
          :background-color (util/get-theme-attribute :table-heading-bgcolor :color3)}]
    [:td {:display "block"}
     [:&:before {:content "attr(data-th)\":\""
                 :font-weight "bold"
                 :margin-right (u/rem 0.5)
                 :display "inline-block"}]
     [:&:last-child:before {:content "attr(data-th)\"\""}]]
    [:th
     :td
     {:text-align "left"
      :padding "0.5em 1em"}]
    [:td:before
     {:color (util/get-theme-attribute :table-text-color)}]
    [:tr {:margin "0 1rem"}
     [:&:hover {:color (util/get-theme-attribute :table-hover-color :table-text-color "#fff")
                :background-color (util/get-theme-attribute :table-hover-bgcolor :color2)}]
     [(s/& (s/nth-child "2n"))
      [:&:hover {:color (util/get-theme-attribute :table-hover-color :table-text-color "#fff")
                 :background-color (util/get-theme-attribute :table-hover-bgcolor :color2)}]
      {:background-color (util/get-theme-attribute :table-stripe-color :table-bgcolor :color1)}]]
    [:td.commands:last-child {:text-align "right"
                              :padding-right (u/rem 1)}]]
   [:.rems-table.cart {:box-shadow :none}]
   [:.inner-cart {:margin (u/em 1)}]
   [:.outer-cart {:border [[(u/px 1) :solid (util/get-theme-attribute :color1)]]
                  :border-radius (u/rem 0.4)}]
   [:.cart-title {:margin-left (u/em 1)
                  :font-weight "bold"}]
   [:.cart-item {:padding-right (u/em 1)}
    [:>span {:display :inline-block :vertical-align :middle}]]
   [:.text-highlight {:color (c/lighten (util/get-theme-attribute :color4) 20)}]))

(def ^:private dashed-form-group {:position "relative"
                                  :border "2px dashed #ccc"
                                  :border-radius (u/rem 0.4)
                                  :padding (u/px 10)
                                  :margin-top 0
                                  :margin-bottom (u/px 16)})

(defn build-screen []
  (list
   (generate-at-font-faces)
   [:* {:margin 0}]
   [:a
    :button
    {:cursor :pointer
     :color (util/get-theme-attribute :link-color :color4)}]
   [:html {:position :relative
           :min-width (u/px 320)
           :height (u/percent 100)}]
   [:body {:font-family (util/get-theme-attribute :font-family "'Lato', sans-serif")
           :min-height (u/percent 100)
           :display :flex
           :flex-direction :column
           :padding-top (u/px 56)}]
   [:h1 :h2 {:font-weight 500}]
   [:h1 :h2 :h3 {:letter-spacing (u/rem 0.17)}]
   [:h4 :h5 :h6 {:letter-spacing (u/rem 0.12)}]
   [:#app {:min-height (u/percent 100)
           :flex 1
           :display :flex}]
   [(s/> :#app :div) {:min-height (u/percent 100)
                      :flex 1
                      :display :flex
                      :flex-direction :column}]
   [:.fixed-top {:background-color "#fff"
                 :border-bottom (util/get-theme-attribute :header-border)
                 :box-shadow (util/get-theme-attribute :header-shadow :table-shadow)
                 :min-height (u/px 56)}]
   [:.main-content {:display :flex
                    :flex-direction :column
                    :flex-wrap :none
                    :min-height (u/px 300)
                    :flex-grow 1}]
   [(s/> :.spaced-sections "*:not(:first-child)") {:margin-top (u/rem 1)}]
   [:.btn-primary
    [:&:hover
     :&:focus
     :&:active:hover
     {:background-color (util/get-theme-attribute :primary-button-hover-bgcolor :primary-button-bgcolor :color4)
      :border-color (util/get-theme-attribute :primary-button-hover-bgcolor :primary-button-bgcolor :color4)
      :color (util/get-theme-attribute :primary-button-hover-color :primary-button-color "#fff")
      :outline-color :transparent}]
    {:background-color (util/get-theme-attribute :primary-button-bgcolor :color4)
     :border-color (util/get-theme-attribute :primary-button-bgcolor :color4)
     :color (util/get-theme-attribute :primary-button-color "#fff")
     :outline-color :transparent}]
   [:.btn-secondary
    [:&:hover
     :&:focus
     :&:active:hover
     {:outline-color :transparent}]]
   [:.btn-primary.disabled :.btn-primary:disabled
    :.btn-secondary.disabled :.btn-secondary:disabled
    {:color "#fff"
     :background-color "#aaa"
     :border-color "#aaa"}]
   [:.button-min-width {:min-width (u/rem 5)}]
   [:.icon-link {:color "#6c757d" ; same colors as .btn-secondary
                 :cursor "pointer"}
    [:&:hover {:color "#5a6268"}]]
   [:.modal--title [:.link
                    {:border-radius "0.25em"
                     :padding "0.25em"
                     :text-align :center
                     :color "#ccc"}
                    [:&:hover {:color (util/get-theme-attribute :color4)
                               :background-color "#eee"}]]]
   [:.alert-info
    (s/descendant :.state-info :.phases :.phase.completed)
    {:color (util/get-theme-attribute :info-color)
     :background-color (util/get-theme-attribute :info-bgcolor)}]
   [:.alert-success
    (s/descendant :.state-approved :.phases :.phase.completed)
    {:color (util/get-theme-attribute :success-color)
     :background-color (util/get-theme-attribute :success-bgcolor)}]
   [:.alert-warning {:color (util/get-theme-attribute :warning-color)
                     :background-color (util/get-theme-attribute :warning-bgcolor)}]
   [:.alert-danger
    :.state-rejected
    (s/descendant :.state-rejected :.phases :.phase.completed)
    {:color (util/get-theme-attribute :danger-color)
     :background-color (util/get-theme-attribute :danger-bgcolor)}]
   [:.nav-link
    :.btn-link
    (s/descendant :.nav-link :a)
    {:color (util/get-theme-attribute :nav-color :link-color :color3)
     :border 0}] ;for button links
   [:.navbar
    [:.nav-link :.btn-link
     {:background-color :inherit}]]
   [:.navbar-top-bar {:width (u/percent 100)
                      :height (u/px 4)
                      :display :flex
                      :flex-direction :row}]
   [:.navbar-top-left {:flex 1
                       :background-color (util/get-theme-attribute :color4)}]
   [:.navbar-top-right {:flex 1
                        :background-color (util/get-theme-attribute :color2)}]
   [:.navbar-toggler {:border-color (util/get-theme-attribute :color1)}]
   [:.nav-link
    :.btn-link
    [:&.active
     {:color (util/get-theme-attribute :nav-active-color :color4)}]
    [:&:hover
     {:color (util/get-theme-attribute :nav-hover-color :color4)}]]
   [:.logo {:height (u/px 140)
            :background-color (util/get-theme-attribute :logo-bgcolor)
            :padding "0 20px"
            :margin-bottom (u/em 1)}]
   [(s/descendant :.logo :.img) {:height "100%"
                                 :background-color (util/get-theme-attribute :logo-bgcolor)
                                 :background-image (resolve-image (util/get-theme-attribute :logo-name))
                                 :-webkit-background-size :contain
                                 :-moz-o-background-size :contain
                                 :-o-background-size :contain
                                 :background-size :contain
                                 :background-repeat :no-repeat
                                 :background-position [[:center :center]]
                                 :background-origin (util/get-theme-attribute :logo-content-origin)
                                 :padding-left (u/px 20)
                                 :padding-right (u/px 20)}]
   [:footer {:width "100%"
             :height (u/px 53.6)
             :color (util/get-theme-attribute :footer-color :table-heading-color "#fff")
             :background-color (util/get-theme-attribute :footer-bgcolor :table-heading-bgcolor :color3)
             :text-align "center"
             :margin-top (u/em 1)}
    [:.navbar {:color (util/get-theme-attribute :footer-color :table-heading-color "#fff")}]]
   [:.jumbotron
    {:background-color "#fff"
     :text-align "center"
     :max-width (u/px 420)
     :margin "30px auto"
     :color "#000"
     :border-style "solid"
     :border-width (u/px 1)
     :box-shadow (util/get-theme-attribute :collapse-shadow :table-shadow)}
    [:h2 {:margin-bottom (u/px 20)}]]
   [:.login-btn {:max-height (u/px 70)
                 :margin-bottom (u/px 20)}
    [:&:hover {:filter "brightness(80%)"}]]
   (generate-rems-table-styles)
   [:.btn.disabled {:opacity 0.25}]
   [:.catalogue-item-link {:color "#fff"
                           :text-decoration "underline"}]
   ;; Has to be defined before the following media queries
   [:.language-switcher {:padding ".5em 0"}]
   (generate-media-queries)
   [:.user
    :.language-switcher
    {:white-space "nowrap"}]
   [(s/descendant :.user :.nav-link) {:display :inline-block}]
   [:.user-name {:text-transform :none}]
   [:.fa
    :.user-name
    {:margin-right (u/px 5)}]
   [:.navbar {:padding-left 0
              :padding-right 0
              :letter-spacing (u/rem 0.015)
              :color (util/get-theme-attribute :navbar-color "#111")}]
   [(s/descendant :.navbar-text :.language-switcher)
    {:margin-right (u/rem 1)}]
   [:.example-page {:margin (u/rem 2)}]
   [(s/> :.example-page :h1) {:margin "4rem 0"}]
   [(s/> :.example-page :h2) {:margin-top (u/rem 8)
                              :margin-bottom (u/rem 2)}]
   [(s/> :.example-page :h3) {:margin-bottom (u/rem 1)}]
   [(s/descendant :.example-page :.example) {:margin-bottom (u/rem 4)}]
   [:.example-content {:border "1px dashed black"}]
   [:.example-content-end {:clear "both"}]
   [:textarea.form-control {:overflow "hidden"}]
   [:.group {:position "relative"
             :border "1px solid #ccc"
             :border-radius (u/rem 0.4)
             :padding (u/px 10)
             :margin-top 0
             :margin-bottom (u/px 16)}]
   [:div.form-control {:height :auto
                       :white-space "pre-wrap"
                       :border-color "rgba(206, 212, 218, 0.2)" ; "#ced4da"
                       :background-color "rgba(0, 0, 0, 0.01)"}
    [:&:empty {:height (u/rem 2.25)}]]
   [:.toggle-diff {:float "right"}]
   [:.diff
    [:ins {:background-color "#acf2bd"}]
    [:del {:background-color "#fdb8c0"}]]
   [:form.inline
    :.form-actions.inline
    {:display :inline-block}
    [:.btn-link
     {:border :none
      :padding 0}]]
   [:.modal-title {:color "#292b2c"}]
   [(s/+
     (s/descendant :.language-switcher :form)
     :form)
    {:margin-left (u/rem 0.5)}]
   [:.commands {:text-align "right"
                :padding "0 1rem"}]
   [".spaced-horizontally > *:not(:first-child)" {:margin-left (u/rem 0.5)}]
   [".spaced-vertically > *:not(:first-child)" {:margin-top (u/rem 0.5)}]
   [".spaced-vertically-3 > *:not(:first-child)" {:margin-top (u/rem 1.5)}]
   [".children-inline-blocks > *" {:display :inline-block}]
   [:.form-group {:text-align "initial"}]
   [:.navbar-flex {:display "flex"
                   :flex-direction "row"
                   :justify-content "space-between"
                   :min-width "100%"}
    [:nav {:flex 1}]]
   [(s/> :.form-actions "*:not(:first-child)")
    (s/> :.commands "*:not(:first-child)")
    {:margin-left (u/em 0.5)}]

   ;; form inputs
   ["input[type=date].form-control" {:width (u/em 12)}]

   ;; workflow editor
   [:.workflow-round dashed-form-group
    [:h2 {:font-weight 500
          :font-size (u/rem 1.4)}]]
   [:.next-workflow-arrow {:position "absolute"
                           :font-size (u/px 40)
                           :left (u/percent 50)
                           :transform "translate(-50%, -1%)"
                           :z-index 1}]
   [:.new-workflow-round {:text-align "center"}]
   [:.remove-workflow-round {:float "right"}]

   ;; form editor
   [:.form-item dashed-form-group]
   [:.form-item-header {:margin-bottom (u/rem 0.5)}
    [:h4 {:display "inline"
          :font-weight "bold"
          :font-size (u/rem 1.1)}]]
   [:.form-item-controls {:float "right"}
    [:* {:margin-left (u/em 0.25)}]]
   [:.new-form-item {:text-align "center"}]

   [:.form-item-option (assoc dashed-form-group
                              :margin-left 0
                              :margin-right 0)]
   [:.new-form-item-option {:text-align "center"}]

   [:.full {:width "100%"}]
   [:.rectangle {:width (u/px 50)
                 :height (u/px 50)}]
   [:.color-1 {:background-color (util/get-theme-attribute :color1)}]
   [:.color-2 {:background-color (util/get-theme-attribute :color2)}]
   [:.color-3 {:background-color (util/get-theme-attribute :color3)}]
   [:.color-4 {:background-color (util/get-theme-attribute :color4)}]
   [:.color-title {:padding-top (u/rem 0.8)}]
   [(s/descendant :.alert :ul) {:margin-bottom 0}]
   [:ul.comments {:list-style-type :none}]
   [:.inline-comment {:font-size (u/rem 1)}]
   [(s/& :p.inline-comment ":last-child") {:margin-bottom 0}]
   [:.inline-comment-content {:display :inline-block}]
   [:.license-panel {:display :inline-block
                     :width "inherit"}]
   [:.card-header.clickable {:cursor "pointer"}]
   [:.rems-card-header {:color (util/get-theme-attribute :table-heading-color "#fff")
                        :background-color (util/get-theme-attribute :table-heading-bgcolor :color3)
                        :margin (u/px -1) ; make sure header overlaps container border
                        }]
   [(s/descendant :.card-header :a) {:color :inherit}]
   ;; hax for opening misalignment
   [:.license-title {:margin-top (u/px 3)}]
   [:.license-block {:color "#000"
                     :white-space "pre-wrap"}]
   [:.collapsing {:-webkit-transition "height 0.1s linear"
                  :-o-transition "height 0.1s linear"
                  :transition "height 0.1s linear"}]
   [:.collapse-toggle {:text-align :center}]
   [:.collapse-wrapper {:border-radius (u/rem 0.4)
                        :border "1px solid #ccc"
                        :background-color (util/get-theme-attribute :collapse-bgcolor "#fff")
                        :box-shadow (util/get-theme-attribute :collapse-shadow :table-shadow)}
    [:.card-title {:color (util/get-theme-attribute :collapse-color "#fff")}]
    [:.card-header {:border-bottom "none"
                    :border-radius (u/rem 0.4)
                    :font-weight 500
                    :font-size (u/rem 1.5)
                    :line-height 1.1
                    :font-family (util/get-theme-attribute :font-family "'Lato', sans-serif")}]]
   [:.collapse-content {:margin (u/rem 1.25)}]
   [:.collapse-wrapper.slow
    [:.collapsing {:-webkit-transition "height 0.25s linear"
                   :-o-transition "height 0.25s linear"
                   :transition "height 0.25s linear"}]]

   [:.color1 {:color (util/get-theme-attribute :color1)}]
   [:.color1-faint {:color (-> (util/get-theme-attribute :color1)
                               (c/saturate -50)
                               (c/lighten 33))}]
   [:h2 {:margin [[(u/rem 1) 0]]}]

   ;; autocomplete, duplicates some Bootstrap styling
   ;; because the component classes are hard-coded
   [:.autocomplete {:width (u/percent 100)}
    [:.autocomplete__control
     [:input {;; from Bootstrap .form-control
              :display :block
              :width (u/percent 100)
              :padding [[(u/rem 0.375) (u/rem 0.75)]]
              :font-size (u/rem 1)
              :line-height 1.5
              :color "#495057"
              :background-color "#fff"
              :background-image :none
              :background-clip :padding-box
              :border [[(u/px 1) :solid "#ced4da"]]
              :border-radius (u/rem 0.25)
              :transition [[:border-color :ease-in-out (u/s 0.15) :box-shadow :ease-in-out (u/s 0.15)]]}]
     ["input:focus" {:color "#495057"
                     :background-color "#fff"
                     :border-color "#80bdff"
                     :outline 0
                     :outline-offset (u/px -2)
                     :box-shadow [[0 0 0 (u/rem 0.2) "rgba(0,123,255,.25)"]]}]]
    [:.autocomplete__selected-items {:display :inline-block}]
    [:.autocomplete__selected-item:last-of-type {:margin-bottom (u/rem 0.5)}]
    [:.autocomplete__selected-item {:height (u/px 40)
                                    :line-height (u/px 40)
                                    :color (util/get-theme-attribute :table-heading-color "#fff")
                                    :background-color (util/get-theme-attribute :table-heading-bgcolor :color3)
                                    :border-radius (u/rem 0.25)
                                    :border [[(u/px 1) :solid "#111"]]}]
    [:.autocomplete__dropdown {:padding (u/px 10)}]
    [:.autocomplete__control [:input {:display :inline-block}]]
    [:.autocomplete__item {:padding (u/px 10)}]
    [:.autocomplete__item--selected {:color (util/get-theme-attribute :table-heading-color "#fff")
                                     :background-color (util/get-theme-attribute :table-heading-bgcolor :color3)}]
    [:.autocomplete__item:hover {:color (util/get-theme-attribute :table-heading-color "#fff")
                                 :background-color (util/get-theme-attribute :table-heading-bgcolor :color3)
                                 :cursor :pointer}]
    [:.autocomplete__selected-item {:display :inline-block
                                    :padding [[0 (u/rem 0.5)]]
                                    :margin-right (u/px 10)}
     [:a.autocomplete__remove-item-button {:margin-left (u/px 5)
                                           :padding (u/rem 0.5)
                                           :padding-right 0
                                           :color (util/get-theme-attribute :danger-color)
                                           :font-weight :bold}]
     [:input {:width (u/percent 100)}]]]

   (generate-phase-styles)
   [(s/descendant :.document :h3) {:margin-top (u/rem 4)}]
   ;; These must be last as the parsing fails when the first non-standard element is met
   (generate-form-placeholder-styles)))

(defstate screen :start (g/css {:pretty-print? false} (build-screen)))
