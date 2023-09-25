(ns app.style
  (:require
   [app.color :as color]))

(def text-shadow (str "1px 1px 2px "  (color/color :highlight) ", -1px -1px 2px " (color/color :shadow)))
(def box-shadow  (str "7px 7px 14px " (color/color :shadow) ", -7px -7px 14px " (color/color :highlight)))

(def text
  {:color       (color/color :text)
   :text-shadow text-shadow})

"#7BC8E6"
