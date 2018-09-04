(ns square-limit.core
  (:require [lemonade.core :as l]
            [lemonade.hosts :as hosts]
            [lemonade.lang :as lang]
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

(defn triangulate
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

(def r2 (/ 1 (math/sqrt 2)))

(defn t [shape a b c depth]
  (if (< 0 depth)
    (let [o (lang/+ a b c)
          mid (lang/+ a (lang/* 0.5 (lang/- o a)))
          diag (lang/- o mid)
          p2 (lang/+ c a)
          s1 (-> shape (l/scale p2 r2) (l/rotate p2 -45) (l/reflect mid diag))
          ;s2 (-> shape (l/scale b r2) (l/rotate b 45) (l/reflect mid diag))
          ]
      (println a b c diag mid o)
      [shape
       (assoc l/line :style {:stroke :hotpink} :from mid :to o)
       (assoc l/circle :radius 5 :centre mid)
       (assoc l/circle :radius 5 :centre o :style {:stroke :none :fill :yellow})
       (assoc l/circle :radius 5 :centre (lang/+ a b) :style {:stroke :none :fill :green})
       s1
       ;; (-> s1 (l/scale o r2) (l/rotate o -45) (l/reflect mid [0 1]))
       (t s1 mid (lang/- b mid) (lang/- o mid) (dec depth))
       #_(t s1 mid (lang/- p2 mid) (lang/- o mid) (dec depth))])
    [])
  )

;; (def f (triangulate b2))

;; (defn rot45
;;   "fn from Henderson's paper"
;;   [t]
;;   (let [p (top-left t)]
;;     (-> t
;;         (l/scale p 0.7071)
;;         (l/rotate p 45))))

;; (def f2
;;   (-> f
;;       (rot45)
;;       (l/reflect [0 1])
;;       (l/translate (bottom-right f))))

;; (def t
;;   [f
;;    f2
;;    (-> f2
;;     (l/rotate 270)
;;     (l/translate (top-left f)))])

;; (def u
;;   (-> (map #(l/rotate f (* % 90)) (range 4))
;;       (l/scale 0.7071)
;;       (l/reflect [0 1])
;;       (l/rotate 45)))

;; REVIEW: Notice here that we're bleeding together aspects of the shape with
;; aspects of the frame. This is because even though f2 is derived geometrically
;; from f, it is a different kind of thing so far as the runtime is
;; concerned. You can ask a tile for its corners, but when you transform a tile,
;; that info gets lost. That's a big problem here.

(def f (triangulate b2))

(def picture
  [(t f  [0 0] [0 250] [250 0] 5)
   (l/with-style {:opacity 0.2 :stroke :blue}
     (t (triangulate (assoc l/line :to [250 0])) [0 0] [0 250] [250 0] 8))
   ])


(defonce host (hosts/default-host {:fullscreen true}))

(defn ^:export init []
  (l/draw! (l/translate picture [300 300]) host))

(defn on-reload []
  (init))
