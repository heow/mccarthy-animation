(ns mccarthy-animation.background
  (:require [quil.core :as quil :include-macros true]))

(def bg-width 960) ; TODO calculate this

(def solid-color -16777216)

;; set of x/y coordinates
;;    #{[1 2] [3 4]}
(defonce plist (atom #{})) 
                                        
(defonce model (atom nil))

;(def image (quil/load-image "resources/background.png"))

(defn get-image-width []
  (if (nil? @model)
    (do ;; always fail the first time, return 0 width as it loads
      (reset! model (quil/load-image "resources/background.png"))
      0)
    (.-width @model)))

;(defn get-image []
;  (if (= 0 (get-image-width))
;    nil
;    @model))

(defn get-pixels []
  (if (= bg-width (get-image-width))
    (quil/pixels @model) 
    []))

;;; this version inspects the image in real-time, it introduces lag
;(comment defn is-pixel-solid? [x y]
;  (if (= bg-width (get-image-width))
;    (quil/get-pixel @model x y) 
;    []))
;
(comment defn is-pixel-solid? [x y]
  false)

(defn is-pixel-solid? [x y]
  (if (zero? (count @plist)) false
      (contains? @plist [x y]) 
      ))

;; This is the most unlispy thing ever designed but an easy way to get x,y
;; it only has to run once.  Don't judge me
(defn work! []
  (let [pixels (get-pixels)
        max (count pixels)
        ]
;    (println " width:  " bg-width)
;    (println " pixels: " max)
    (doall ;; force it to calculate x,y
      (dotimes [i (count (take max pixels))]
        (let [pixel (nth pixels i)              
              y (int (/ i bg-width))
              x (- i (* y bg-width))]
;          (println "pixel " pixel)
          (if (= solid-color pixel)
            (swap! plist conj [x y])
            )))) ))

(defn do-work! []
  (if (empty? @plist)
    (doall
      (reset! plist #{})
      (work!)))
  @plist)

