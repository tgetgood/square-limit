(ns square-limit.core
  (:require [falloleen.core :as l]
            [falloleen.hosts :as hosts]
            [falloleen.lang :as lang]
            [falloleen.math :as math]))

(enable-console-print!)

(def base
  (l/path
   [(assoc l/line  :to [50 60])
    (assoc l/bezier :from [50 60] :to [150 15]
           :c1 [55 64] :c2 [101 54])
    (assoc l/bezier :from [150 15]  :to [250 0]
           :c1 [153 30] :c2 [220 10])]))

(def sqr2 (/ 1 (math/sqrt 2)))

(defn with-frames [xs]
  (map (fn [x] [x (l/frame x)]) xs))

(defn triangulate [p]
  [p
   (-> p
       (l/scale :bottom-right sqr2)
       (l/rotate :bottom-right 45)
       (l/reflect :bottom-right [1 0]))
   (l/rotate p 90)
   (-> p
       (l/rotate 90)
       (l/scale :bottom-right sqr2)
       (l/rotate :bottom-right -45)
       (l/reflect :bottom-right [0 1]))])

(def fish-outline (triangulate base))

(l/deftemplate fish
  {:curve base}
  (triangulate curve)
  lang/Bounded
  (extent [this]
    (let [{:keys [origin a]} (l/frame curve)
          w (first a)]
      (l/coordinate-frame origin [w 0] [0 w])))

  lang/Affine
  (transform [this xform f]
    (Fish. (lang/transform curve xform f))))

(defn most [pick rf]
  (fn [{:keys [origin a b]}]
    (rf (pick origin) (reduce + (map pick [origin a b])))))

(def left-most (most first min))
(def right-most (most first max))
(def top-most (most second max))
(def bottom-most (most second min))

(defn above
  ([] [])
  ([a b]
   (let [delta (- (top-most (l/frame b)) (bottom-most (l/frame a)))]
     [b (l/translate a [0 delta])])))

(defn beside
  ([] [])
  ([a b]
   (let [delta (- (right-most (l/frame a)) (left-most (l/frame b)))]
     [(l/translate b [delta 0]) a])))

(defn matrix-grid
  [m]
  (reduce above
          (map #(reduce beside %) m)))

(defn q [a b c d]
  (l/scale (matrix-grid [[a b] [c d]]) 0.5))

(defn rot45 [shape]
  (-> shape
      (l/scale :top-left sqr2)
      (l/rotate :top-left 45)))

(def fish2
  (-> fish
      rot45
      (l/reflect :centre [0 1])))

(l/deftemplate t
  {:shape fish}
  (let [s2 (-> shape rot45 (l/reflect :centre [0 1]) )]
    [shape
     s2
     (-> s2 (l/rotate :bottom-left 270))])

  lang/Bounded
  (extent [_] (l/frame shape)))

(l/deftemplate u
  {:shape fish2}
  (map #(l/rotate shape :bottom-left (* % 90)) (range 4))

  lang/Bounded
  (extent [_]
    ;; FIXME: You shouldn't have to set this by hand.
    (l/coordinate-frame [0 0] [250 0] [0 250])))

(def blank (l/style t {:stroke :none :opacity 0.3}))

(def side1 (q blank blank (l/rotate t :centre 90) t))

(def side2 (q side1 side1 (l/rotate t :centre 90) t))

(def c1 (q blank blank blank u))

(def c2 (q c1 side1 (l/rotate side1 :centre 90) u))

(def square-limit2
  (matrix-grid
       [[c2 side2 (l/rotate c2 :centre 270)]
        [(l/rotate side2 :centre 90) u (l/rotate side2 :centre 270)]
        [(l/rotate c2 :centre 90) (l/rotate side2 :centre 180)
         (l/rotate c2 :centre 180)]]))

(declare side)

(defn side* [n]
  (if (zero? n)
    blank
    (let [sn-1 (side (dec n))]
      (q sn-1 sn-1 (l/rotate t :centre 90) t))))

(def side (memoize side*))

(defn corner [n]
  (if (zero? n)
    blank
    (let [cn-1 (corner (dec n))
          sn-1 (side (dec n))]
      (q cn-1 sn-1 (l/rotate sn-1 :centre 90) u))))

(defn square-limit [n]
  (let [c (corner n)
        s (side n)]
    (matrix-grid
       [[c s (l/rotate c :centre 270)]
        [(l/rotate s :centre 90) u (l/rotate s :centre 270)]
        [(l/rotate c :centre 90) (l/rotate s :centre 180)
         (l/rotate c :centre 180)]])))

(def image
  (-> (square-limit 6)
      (l/translate [200 200])))

(defonce host (hosts/default-host {:size :fullscreen}))


(defn ^:export init []
  (l/draw! image
           host))

(defn on-reload []
  (init))
