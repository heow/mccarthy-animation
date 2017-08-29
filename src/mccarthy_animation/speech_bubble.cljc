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

(defn draw [hero prewrapped-text]
  (if (not (empty? prewrapped-text))
    (do
      (quil/stroke 255)
      (quil/fill   255)
      (quil/text-size 12)
      (quil/text-align :left)

      (let [hero-x (:x (:position hero))
            hero-y (:y (:position hero))
            hero-size-x (:x (:size hero))
            hero-size-y (:y (:size hero))
            text-to-say (wrap-line 20 prewrapped-text)
            text-width  (quil/text-width text-to-say)
            lines-of-text (+ 1 (count (filter #(= "\n" %) text-to-say)))
            box-size-y (+ 20 (* 13 lines-of-text))]
    
        (quil/rect (- (+ hero-x hero-size-x) 35)
                   (+ (- hero-y hero-size-y) (- 50 box-size-y))
                   (+ 10 text-width) box-size-y)

        (quil/triangle (+ hero-x 40) (- hero-y 0)
                       (min (+ (+ 10 text-width) (- (+ hero-x hero-size-x) 35)) (+ hero-x 70)) (- hero-y 20)
                       (min (+ hero-x 50) (+ 25 hero-x text-width)) (- hero-y 20))

        (quil/fill 0) ; blacken text
        (quil/text text-to-say (- (+ hero-x hero-size-x) 30) (+ (- hero-y hero-size-y) (- 70 box-size-y)))
        
        (quil/stroke config/default-stroke-color) ;; reset to defaults
        (quil/fill   config/default-fill-color) ))))
