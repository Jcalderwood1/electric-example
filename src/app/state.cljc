(ns app.state)

#?(:cljs (def !db (atom {:notification nil
                         :selected-tab :home})))

#?(:cljs (defn update-db!
           [path value]
           (swap! !db  assoc-in path value)))
