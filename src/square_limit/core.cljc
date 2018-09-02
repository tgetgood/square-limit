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

(def top-left
  [(assoc l/bezier :to [-5 -100] :c1 [-3 -5] :c2 [-25 -90])
   (assoc l/bezier :from [-5 -100] :to [-60 -200]
          :c1 [-9 -105] :c2 [-61 -199])
   (assoc l/line :from [-60 -200] :to [0 -250])])

(def top-right
  (-> top-left
      (l/reflect [0 1])
      (l/scale 0.7071)
      (l/rotate 45)))

(def bottom-left
  (l/rotate top-left [0 -250] -90))

(def fish
  [top-left
   bottom-left
   top-right
   (-> bottom-left
       (l/reflect [250 -250] [ 0 1])
       (l/scale [250 -250] 0.7071)
       (l/rotate [250 -250] 135))])

(def f2
  (-> fish
      (l/scale 0.7071)
      (l/rotate 45)
      (l/reflect [0 1])))

(def f3
  (l/rotate f2 270))


(def picture
  [fish f2 f3])

(defonce host (hosts/default-host {:fullscreen true}))

(defn ^:export init []
  (l/draw! (l/translate picture [300 300]) host))

(defn on-reload []
  (init))
