(ns mccarthy-animation.ball
  #?(:clj (:gen-class))
  (:require [quil.core :as quil :include-macros true]))

(defonce travel-speed 0.3)
(defonce angle-speed 0.01)

(defn create [name max-x max-y]
  {:position {:x (rand-int max-x) :y (rand-int max-y)}
   :orbit-angle-offset (rand-int 360)})

(defn calculate-orbit-target [angle hero ball]
  (let [angle (+ angle (:orbit-angle-offset ball))
        target-x (+ (* 50 (quil/cos angle))  ; elipse
                    (:x (:position hero))    ; hero x
                    (/ (:x (:size hero)) 2)) ; 50% of hero y
        target-y (+ (* 20 (quil/sin angle))
                    (:y (:position hero)))]
    {:x target-x :y target-y } ))

(defn aim-at [me-pos target-pos]
  (let [me-x (:x me-pos) ;; more readable
        me-y (:y me-pos)
        tg-x (:x target-pos)
        tg-y (:y target-pos)

        ;; do the math
        target-angle (quil/atan2  (quil/abs (- tg-y me-y)) (quil/abs (- tg-x me-x)))
        new-x (if (> tg-x me-x) (+ me-x (* travel-speed (quil/cos target-angle))) (- me-x (* travel-speed (quil/cos target-angle))))
        new-y (if (> tg-y me-y) (+ me-y (* travel-speed (quil/sin target-angle))) (- me-y (* travel-speed (quil/sin target-angle))))
        ]
    {:x new-x :y new-y}))
