(ns square-limit.core
  (:require [falloleen.core :as l]
            [falloleen.hosts :as hosts]
            [falloleen.lang :as lang]
            [falloleen.math :as math]))

(enable-console-print!)

(def base
  (l/path
   [(assoc l/bezier :to [-5 -100] :c1 [-3 -5] :c2 [-25 -90])
    (assoc l/bezier :from [-5 -100] :to [-60 -200]
           :c1 [-9 -105] :c2 [-61 -199])
    (assoc l/line :from [-60 -200] :to [0 -250])]))

(def b2
  (l/path
   [(assoc l/line  :to [50 60])
    (assoc l/bezier :from [50 60] :to [150 15]
           :c1 [55 64] :c2 [101 54])
    (assoc l/bezier :from [150 15]  :to [250 0]
           :c1 [153 30] :c2 [220 10])]))

;; (defn triangulate
;;   "Given a path, creates a base tile with the appropriate symmetry.
;;   Path is assumed to begin at the origin, and cover the bottom portion of the
;;   tiling triangle (goes to the right)."
;;   [path]
;;   (let [[_ right-v] (l/endpoints path)
;;         left-v      (rotc right-v)]
;;     [path
;;      (-> path
;;          (l/reflect right-v [1 0])
;;          (l/scale right-v 0.7071)
;;          (l/rotate right-v -45))
;;      (-> path
;;          (l/rotate 90)
;;          (l/reflect left-v [0 1])
;;          (l/rotate left-v 45)
;;          (l/scale left-v 0.7071))
;;      (l/rotate path 90)]))

;; (defn t [shape a b c depth]
;;   (if (< 0 depth)
;;     (let [o (lang/+ a b c)
;;           mid (lang/+ a (lang/* 0.5 (lang/+ b c)))
;;           diag (lang/- o mid)
;;           p2 (lang/+ c a)
;;           s1 (-> shape (l/scale p2 r2) (l/rotate p2 -45) (l/reflect mid diag))
;;           ;s2 (-> shape (l/scale b r2) (l/rotate b 45) (l/reflect mid diag))
;;           ]
;;       (println a b c diag mid o)
;;       [shape
;;        (assoc l/line :style {:stroke :hotpink} :from mid :to o)
;;        (assoc l/circle :radius 5 :centre mid)
;;        (assoc l/circle :radius 5 :centre o :style {:stroke :none :fill :yellow})
;;        (assoc l/circle :radius 5 :centre (lang/+ a b) :style {:stroke :none :fill :green})
;;        s1
;;        ;; (-> s1 (l/scale o r2) (l/rotate o -45) (l/reflect mid [0 1]))
;;        (t s1 mid (lang/- b mid) (lang/- o mid) (dec depth))
;;        #_(t s1 mid (lang/- p2 mid) (lang/- o mid) (dec depth))])
;;     [])
;;   )

;; (defn syml [shape a b c]
;;   (let [p (lang/+ b a)]
;;     (-> shape
;;         (l/scale p r2)
;;         (l/rotate p 45)
;;         (l/reflect (lang/+ a (lang/* 0.5 (lang/+ b c))) [0 1]))))

;; (defn symr [shape a b c]
;;   (let [p (lang/+ b a)]
;;     (-> shape
;;         (l/scale p r2)
;;         (l/rotate p 45)
;;         (l/reflect (lang/+ a (lang/* 0.5 (lang/+ b c))) (lang/+ a (lang/* 0.5 (lang/+ b c)))))))

;; ;; REVIEW: Notice here that we're bleeding together aspects of the shape with
;; ;; aspects of the frame. This is because even though f2 is derived geometrically
;; ;; from f, it is a different kind of thing so far as the runtime is
;; ;; concerned. You can ask a tile for its corners, but when you transform a tile,
;; ;; that info gets lost. That's a big problem here.

;; (def f (triangulate b2))

;; (def picture
;;   [f
;;    (syml f [0 0] [0 250] [250 0])
;;    (symr f [0 0] [0 250] [250 0])]
;;   #_[(t f  [0 0] [0 250] [250 0] 5)
;;    (l/with-style {:opacity 0.2 :stroke :blue}
;;      (t (triangulate (assoc l/line :to [250 0])) [0 0] [0 250] [250 0] 8))
;;    ])


(def sqr2 (/ 1 (math/sqrt 2)))

(defn with-frames [xs]
  (map (fn [x] [x (l/frame x)]) xs))

(defn triangulate [p]
  [p
   (-> p
       (l/rotate :bottom-right 45)
       (l/reflect :top-right [1 0])
       (l/scale :bottom-right sqr2))
   (l/rotate p 90)
   (-> p
       (l/rotate 90)
       (l/scale :top-right sqr2)
       (l/reflect :right [0 1])
       (l/rotate :top-left 45))])

(def fish-outline (triangulate b2))

(l/deftemplate fish
  {:curve b2}
  (triangulate curve)
  lang/Framed
  (frame [this]
    (let [{:keys [origin width]} (l/frame curve)]
      (assoc l/rectangle :origin origin :width width :height width)))

  lang/Affine
  (transform [this xform f]
    (Fish. (lang/transform curve xform f))))

(defn above [a b]
  [(l/scale b [1 0.5])
   (-> a
       (l/translate [:relative [0 0.5]])
       (l/scale :bottom-left [1 0.5]))])

(defn beside [a b]
  [(l/scale a [0.5 1])
   (-> b
       (l/translate [:relative [0.5 0]])
       (l/scale :bottom-right [0.5 1]))])

(defn q [a b c d]
  (above (beside a b) (beside c d)))

(def fish2
  (-> fish
      (l/reflect :centre [0 1])
      (l/rotate :top-right -45)
      (l/scale [250 250] sqr2)
))

(def t
  [fish
   fish2
   (l/rotate fish2 [125 125] 270)])

(def u
  (map #(l/rotate fish (* % 90)) (range 4)))

(def image
  [(->  (q t t t t)
        ;; with-frames
     (l/translate [300 300]))])



(defonce host (hosts/default-host {:size :fullscreen}))

(defn ^:export init []
  (l/draw! image
           host))

(defn on-reload []
  (init))
