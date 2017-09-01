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
(defn wrap-line [size text]
  (apply str (drop-last ;; remove final LF
              (apply str (map #(str % "\n") (wrap-line-work size text))))))

(defn reverse-color [reverse? color]
  (cond (= reverse? false) color
        (= color config/white) config/black
        (= color config/black) config/white
        :else color))

(defn draw
  ([hero raw-text] (draw hero raw-text false))
  ([hero raw-text reverse?]
   (if (not (empty? raw-text))
     (do
       (quil/stroke (reverse-color reverse? config/white))
       (quil/fill   (reverse-color reverse? config/white))
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
         (let [stroke-weight 2
               box-x     (- (+ hero-x hero-size-x) 20)
               box-y     (+ (- hero-y hero-size-y) (- 30 box-height))
               box-width (+ 16 text-width)]
           
           (quil/rect box-x box-y box-width box-height)
           
           ;; outline box on 3 sides
           (quil/stroke (reverse-color reverse? config/black))
           (quil/stroke-weight stroke-weight)
           (quil/line box-x box-y (- (+ box-x box-width) stroke-weight) box-y)
           (quil/line (+ box-x box-width) box-y (+ box-x box-width) (- (+ box-y box-height) 1))
           (quil/line box-x box-y box-x (- (+ box-y box-height) 1))

           ;; triangle
           (quil/stroke (reverse-color reverse? config/white))
           (let [tri-0-x (+  hero-x (/ hero-size-x 2) (* hero-size-x 0.2))
                 tri-0-y (- hero-y 5)
                 tri-1-x (- (+ box-x (/ box-width 2)) 0)
                 tri-y   (+ box-y box-height)
                 tri-2-x (- (+ box-x (/ box-width 2)) (* box-width 0.25))]

             (quil/triangle tri-0-x tri-0-y
                            tri-1-x tri-y
                            tri-2-x tri-y)

             ;; outline triangle
             (quil/stroke (reverse-color reverse? config/black))
             (quil/line tri-0-x tri-0-y tri-1-x (+ tri-y 1))
             (quil/line tri-0-x tri-0-y tri-2-x (+ tri-y 1))

             ;; last little bits to tie in triangle and box
             (quil/line box-x (+ box-y box-height) (- tri-2-x 1) tri-y)
             (quil/line (- (+ box-x box-width) stroke-weight) (+ box-y box-height) (+ tri-1-x 1) tri-y) )

           ;; actual text
           (quil/fill (reverse-color reverse? config/black)) ; blacken text
           (quil/text text-to-say (+ box-x 8) (+ box-y 20)) )

         ;; reset to defaults
         (quil/stroke (reverse-color reverse? config/default-stroke-color)) 
         (quil/fill   (reverse-color reverse? config/default-fill-color)) )))) )
