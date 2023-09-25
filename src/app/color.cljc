(ns app.color)

(def light-theme
  {:button               "#ecf0f3"
   :base                 "#e9edf0"
   :highlight            "#ffffff"
   :shadow               "#d1d9e6"
   :text                 "#596a72"
   :call-to-action       "#dc5078"
   :call-to-action-text  "#ffffff"})

(def original-theme
  {:button              "#e0e0e0"
   :base                "#dcdcdc"
   :highlight           "#ffffff"
   :shadow              "#b8b8b8"
   :text                "#8C8C8C"
   :call-to-action      "#DC5078"
   :call-to-action-text "#ffffff"})

(def dark-theme
  {:button               "#2c2c2c"
   :base                 "#333333"
   :highlight            "#4d4d4d"
   :shadow               "#1a1a1a"
   :text                 "#c4c4c4"
   :call-to-action       "#e43f5a"
   :call-to-action-text  "#ffffff"})

(defn color
  [color]
  (get light-theme color))
