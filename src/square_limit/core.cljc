(ns square-limit.core
  (:require [lemonade.core :as l]
            [lemonade.hosts :as hosts]
            [lemonade.math :as math]))

#?(:cljs (enable-console-print!))

(l/deftemplate path*
  {:segments []}
  segments
  l/ISegment
  (endpoints [_]
    (when (seq segments)
      [(first (l/endpoints (first segments)))
       (second (l/endpoints (last segments)))]))
  (contiguous [_]
    (let [endpoints (map l/endpoints segments)]
      (every? (fn [[_ e] [s _]] (= e s))
              (partition 2 (interleave endpoints (rest endpoints)))))))

(defprotocol IT
  (top-left [_])
  (bottom-right [_]))

(defn path [segs]
  (assoc path* :segments segs))

(def base
  (path [(assoc l/bezier :to [-5 -100] :c1 [-3 -5] :c2 [-25 -90])
          (assoc l/bezier :from [-5 -100] :to [-60 -200]
                 :c1 [-9 -105] :c2 [-61 -199])
          (assoc l/line :from [-60 -200] :to [0 -250])]))

(def b2
  (path [(assoc l/line  :to [50 60])
          (assoc l/bezier :from [50 60] :to [150 15]
                 :c1 [55 64] :c2 [101 54])
         (assoc l/bezier :from [150 15]  :to [250 0]
                :c1 [153 30] :c2 [220 10])]))

(defn rotc
  [[x y]]
  [(- y) x])

(defn triangluate
  "Given a path, creates a base tile with the appropriate symmetry.
  Path is assumed to begin at the origin, and cover the bottom portion of the
  tiling triangle (goes to the right)."
  [path]
  (let [[_ right-v] (l/endpoints path)
        left-v      (rotc right-v)]
    [path
     (-> path
         (l/reflect right-v [1 0])
         (l/scale right-v 0.7071)
         (l/rotate right-v -45))
     (-> path
         (l/rotate 90)
         (l/reflect left-v [0 1])
         (l/rotate left-v 45)
         (l/scale left-v 0.7071))
     (l/rotate path 90)]))

(l/deftemplate tile
  {:base l/line}
  ;;TODO:
  (triangluate base)
  IT
  (top-left [_]
    (rotc (second (l/endpoints base))))
  (bottom-right [_]
    (second (l/endpoints base))))

(def f (assoc tile :base b2))

(defn rot45
  "fn from Henderson's paper"
  [t]
  (let [p (top-left t)]
    (-> t
        (l/scale p 0.7071)
        (l/rotate p 45))))

(def f2
  (-> f
      (rot45)
      (l/reflect [0 1])
      (l/translate (bottom-right f))))

(def t
  [f
   f2
   (-> f2
    (l/rotate 270)
    (l/translate (top-left f)))])

(def u
  (-> (map #(l/rotate f (* % 90)) (range 4))
      (l/scale 0.7071)
      (l/reflect [0 1])
      (l/rotate 45)))

;; REVIEW: Notice here that we're bleeding together aspects of the shape with
;; aspects of the frame. This is because even though f2 is derived geometrically
;; from f, it is a different kind of thing so far as the runtime is
;; concerned. You can ask a tile for its corners, but when you transform a tile,
;; that info gets lost. That's a big problem here.

(def picture
  [u
   (l/translate t [165 -30])])


(defonce host (hosts/default-host {:fullscreen true}))

(defn ^:export init []
  (l/draw! (l/translate picture [300 300]) host))

(defn on-reload []
  (init))
