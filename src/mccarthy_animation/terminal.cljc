(ns mccarthy-animation.terminal
  (:require [quil.core                          :as quil :include-macros true]
            [mccarthy-animation.config          :as config]
            [mccarthy-animation.speech-bubble   :as speech]
            [mccarthy-animation.character       :as char]))

(defn create [pos-x pos-y]
  {:id (gensym "term-")
   :image (quil/load-image "resources/terminal.png")
   :position {:x pos-x :y pos-y}
   :size {:x 64 :y 57} ;; TODO calculate this
   })

(defn draw [term bump?]
  (quil/with-translation [(:x (:position term)) (:y (:position term))]
    (quil/fill config/black)
    ;; first layer screen background
    (quil/rect 10 5 40 20) 
    (quil/fill config/white)

    ;; second layer text
    (if bump?
      (do
        (quil/text-size 10)
        (quil/text "foo" 15 21))
      (do
        (quil/text-size 4)
        (quil/text (speech/wrap-line 20 (char/select-speech-randomly)) 10 15)))
    
    ;; third layer is image, with "hole" for screen
    (quil/image (:image term) 0 0 (:x (:size term)) (:y (:size term))) )

  ;; sure, make it talk
  ;(speech/draw term "you have mail" true)
  )
