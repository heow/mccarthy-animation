(ns mccarthy-animation.config)

(defonce background-color     240)
(defonce screen-size          {:x 320 :y 320})
(defonce screen-width         (:x screen-size))
(defonce frame-rate           30)

(defonce default-font-size 18)
(defonce font "sans-serif") ; built-in fonts: sans-serif serif monopace fantasy cursive

;; TODO once loadable fonts work in CLJS
;(defonce font "resources/PressStart2P-Regular.ttf")
;(defonce font "resources/VT323-Regular.ttf")

(defonce default-stroke-color 0)
(defonce default-fill-color   255)

(defonce background-scroll-speed 5)
(defonce background-max-width (* 3 screen-width))

(defonce hero-init-position {:x 140 :y 250})
(defonce hero-size          {:x 64  :y 64}) ; don't change these yet
