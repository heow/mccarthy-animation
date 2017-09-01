(ns mccarthy-animation.config)

(defonce screen-size          {:x 320 :y 320})
(defonce screen-width         (:x screen-size))
(defonce frame-rate           30)

(defonce default-font-size 18)

;; TODO once loadable fonts work in CLJS
;(defonce font "sans-serif") ; built-in fonts: sans-serif serif monopace fantasy cursive
(defonce font "resources/PressStart2P-Regular.ttf")
;(defonce font "resources/VT323-Regular.ttf")

(defonce background-color     240)
(defonce white                255)
(defonce black                0)

(defonce default-stroke-color 0)
(defonce default-fill-color   255)

(defonce background-scroll-speed 5)
(defonce background-max-width (* 3 screen-width))
(defonce background-scroll-point 0.15) ;; as % of screen

(defonce hero-init-position {:x 140 :y 250})

;; ensure any additions are reflected in sketch start for cljs and clj
(defonce sketch-opts
  {:host "mccarthy-animation"
   :size [(:x screen-size) (:y screen-size)]
   :no-start true ; disable autostart
   :title "McCarthy Animation"})

(def scroll-point-right
  (- screen-width
     (* screen-width background-scroll-point)))

(def scroll-point-left
  (* screen-width background-scroll-point)
  )
