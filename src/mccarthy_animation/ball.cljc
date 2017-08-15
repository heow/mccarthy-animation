(ns mccarthy-animation.ball
  #?(:clj (:gen-class))
  (:require [quil.core :as quil :include-macros true]))

(defonce speed 1.5)

(defn aim-at [me-pos target-pos]
  (let [speed 1.5
        me-x (:x me-pos)
        me-y (:y me-pos)
        tg-x (:x target-pos)
        tg-y (:y target-pos)
        target-angle (quil/atan2  (quil/abs (- tg-y me-y)) (quil/abs (- tg-x me-x)))
        new-x (if (> tg-x me-x) (+ me-x (* speed (quil/cos target-angle))) (- me-x (* speed (quil/cos target-angle))))
        new-y (if (> tg-y me-y) (+ me-y (* speed (quil/sin target-angle))) (- me-y (* speed (quil/sin target-angle))))
        ]

    ;;{:x ball-new-x :y ball-new-y}
    [ new-x new-y ] ))
