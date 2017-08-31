(ns mccarthy-animation.speech-bubble
  (:require [mccarthy-animation.config :as config]
            [quil.core :as quil :include-macros true]))

;; wordy but it actually works
;; "long text" => ("long" "text")
(defn- wrap-line-work [size text]
  (loop [left size line [] lines []
              words (clojure.string/split text #"\s+")]
         (if-let [word (first words)]
           (let [wlen (count word)
                 spacing (if (== left size) "" " ")
                 alen (+ (count spacing) wlen)]
             (if (<= alen left)
               (recur (- left alen) (conj line spacing word) lines (next words))
               (recur (- size wlen) [word] (conj lines (apply str line)) (next words))))
           (when (seq line)
             (conj lines (apply str line))))))

;; for readability
(defn- wrap-line [size text]
  (apply str (drop-last ;; remove final LF
              (apply str (map #(str % "\n") (wrap-line-work size text))))))

(defn draw [hero raw-text]
  (if (not (empty? raw-text))
    (do
      (quil/stroke 255)
      (quil/fill   255)
      (quil/text-size 12)
      (quil/text-align :left)

      (let [hero-x        (:x (:position hero))
            hero-y        (:y (:position hero))
            hero-size-x   (:x (:size hero))
            hero-size-y   (:y (:size hero))
            text-to-say   (wrap-line 20 raw-text)
            text-width    (quil/text-width text-to-say)
            lines-of-text (+ 1 (count (filter #(= "\n" %) text-to-say)))
            box-height    (+ 18 (* 14 lines-of-text))]

        ;; box
        (let [box-x     (- (+ hero-x hero-size-x) 35)
              box-y     (+ (- hero-y hero-size-y) (- 50 box-height))
              box-width (+ 16 text-width)]
          (quil/rect box-x box-y box-width box-height)

          ;; outline box on 3 sides
          (quil/stroke 0)
          (quil/line box-x box-y (+ box-x box-width) box-y)
          (quil/line (+ box-x box-width) box-y (+ box-x box-width) (+ box-y box-height))
          (quil/line box-x box-y box-x (+ box-y box-height))

          ;; triangle
          (quil/stroke 255)
          (let [tri-0-x (+ hero-x 40) 
                tri-0-y (- hero-y 0)
                tri-1-x (min (+ (+ 5 text-width) box-x) ; normal
                             (+ hero-x 70)) ; small
                tri-y   (+ box-y box-height)
                tri-2-x (min (+ hero-x 45) ; normal
                             (+ 25 hero-x text-width))] ; small

            (quil/triangle tri-0-x tri-0-y
                           tri-1-x tri-y
                           tri-2-x tri-y)

            ;; outline triangle
            (quil/stroke 1)
            (quil/line tri-0-x tri-0-y tri-1-x tri-y)
            (quil/line tri-0-x tri-0-y tri-2-x tri-y)

            ;; last little bits to tie in triangle and box
            (quil/line box-x (+ box-y box-height) tri-2-x tri-y)
            (quil/line (+ box-x box-width) (+ box-y box-height) tri-1-x tri-y) )

          ;; actual text
          (quil/fill 0) ; blacken text
          (quil/text text-to-say (+ box-x 8) (+ box-y 20)) )

        
        (quil/stroke config/default-stroke-color) ;; reset to defaults
        (quil/fill   config/default-fill-color) ))))
