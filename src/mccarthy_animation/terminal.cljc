(ns mccarthy-animation.terminal
  (:require [quil.core                          :as quil :include-macros true]
            [mccarthy-animation.config          :as config]
            [mccarthy-animation.speech-bubble   :as speech]
            [mccarthy-animation.character       :as char]))

(defn create [pos-x pos-y size-x size-y]
  {:id (gensym "term-")
   :image (quil/load-image "resources/terminal.png")
   :position {:x pos-x :y pos-y}
   :size {:x size-x :y size-y}})

(defn draw [term bump?]
  (quil/with-translation [(:x (:position term)) (:y (:position term))]
    (quil/fill config/black)
    ;; first layer screen background
    (quil/rect 10 5 20 20) 
    (quil/fill config/white)

    ;; second layer text
    (if bump?
      (do
        (quil/text-size 10)
        (quil/text "hi" 15 20))
      (do
        (quil/text-size 4)
        (quil/text (speech/wrap-line 20 (char/select-speech-randomly)) 10 15)))
    
    ;; third layer is image, with "hole" for screen
    (quil/image (:image term) 0 0 (:x (:size term)) (:y (:size term)))) )
