(ns app.tabs
  (:require
   [app.color :as color]
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def tab-style
  (merge style/text
         {:cursor        "pointer"
          :padding       "10px"
          :font-size     "24px"
          :border-bottom "none"}))

(def active-tab-style
  (merge tab-style
         {:border-bottom (str "3px solid " (color/color :call-to-action))}))

(e/defn Tab
  [tab-name !active-tab active-tab]
  (dom/div
    (dom/style
     (if (= active-tab tab-name)
       active-tab-style
       tab-style))
    (dom/on "click"
            (e/fn [_]
              (dom/style
               (if (= active-tab tab-name)
                 active-tab-style
                 tab-style))
              (reset! !active-tab tab-name)))
    (dom/text tab-name)))

(e/defn TabContainer
  [tabs]
  (let [default-tab (first (keys tabs))
        !active-tab (atom default-tab)
        active-tab  (e/watch !active-tab)]
    (dom/div
      (dom/style
       {:padding "20px"})
      (dom/div
        (dom/style {:display "flex"})
        (e/for-by first
            [tab-name (keys tabs)]
            (Tab. tab-name !active-tab active-tab)))
      (let [body (get tabs active-tab)]
        (dom/div
          (dom/style
           {:margin-top "20px"
            :width      "600px"})
          (body.))))))
