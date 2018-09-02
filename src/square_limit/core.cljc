(ns square-limit.core
    (:require [lemonade.core :as l]
              [lemonade.hosts :as hosts]))

#?(:cljs (enable-console-print!))

(defonce app-db (atom {:text "Almost Useless"
                       :count 3}))

(defn render [state]
  (let [{:keys [text count]} state]
    [(-> l/text
         (assoc :text text)
         (l/scale 4)
         (l/translate [250 550]))
     (map (fn [i] (l/translate
                   (assoc l/circle :radius 100)
                   [(* (inc i) 200) 400]))
          (range count))]))

(defn ^:export init []
  (l/draw! (render @app-db) (hosts/default-host {:fullscreen true})
   ))

(defn on-reload []
  (init))
