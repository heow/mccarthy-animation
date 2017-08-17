(ns mccarthy-animation.ball
  #?(:clj (:gen-class))
  (:require [quil.core :as quil :include-macros true]))

(defonce speed 0.3)

(defn create [name max-x max-y]
  {:position {:x (rand-int max-x) :y (rand-int max-y)} })

(defn aim-at [me-pos target-pos]
  (let [;; just make things more readable
        me-x (:x me-pos)
        me-y (:y me-pos)
        tg-x (:x target-pos)
        tg-y (:y target-pos)

        ;; do the math
        target-angle (quil/atan2  (quil/abs (- tg-y me-y)) (quil/abs (- tg-x me-x)))
        new-x (if (> tg-x me-x) (+ me-x (* speed (quil/cos target-angle))) (- me-x (* speed (quil/cos target-angle))))
        new-y (if (> tg-y me-y) (+ me-y (* speed (quil/sin target-angle))) (- me-y (* speed (quil/sin target-angle))))
        ]
    ;;[ new-x new-y ]
    {:x new-x :y new-y}))
