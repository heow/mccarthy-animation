(ns mccarthy-animation.core
  #?(:clj (:gen-class))
  #?(:clj (:use [clojure.pprint]))
  (:require [quil.core                          :as quil :include-macros true]
            [quil.middleware                    :as quilm]
            [mccarthy-animation.character       :as char]
            [mccarthy-animation.ball            :as ball]
            [mccarthy-animation.lispm           :as lispm]
            [original-lisp.core                 :as lisp]
            [mccarthy-animation.speech-bubble   :as bubble]
            [mccarthy-animation.config          :as config])
  #?(:clj (:require [clojure.tools.nrepl.server :as nrepl])))

(defn say [list-of-symbols]
  (apply str (interpose " " list-of-symbols)))

(defn move-up [distance]
  (str "moving up " distance " ..."))

(defn setup []
  (quil/frame-rate config/frame-rate)
  (quil/color-mode :hsb) ; Set color mode to HSB (HSV) instead of default RGB.
  
  (quil/text-font (quil/create-font config/font config/default-font-size true))

  ;; setup function returns initial state. It contains
  ;; circle color and position.
  {:color 0
   :angle 0
   :bg (quil/load-image "resources/background.png")
   :hero (char/create "fooman" config/hero-init-x config/hero-init-y)
   :balls (repeatedly (+ 2 (rand-int 5)) #(ball/create "ball" (:x config/screen-size) (:y config/screen-size)))
   :lisp-result ""
   :lisp-time 0 })

(defn now [] (quil/millis))

(defn- get-keystroke-or-mouse []
  (cond (quil/key-pressed?) (quil/key-as-keyword)
        (quil/mouse-pressed?) :mouse-click
        :else :none))

;; Hack to determine when we want to evaluate some lisp, checks to see what we are
;; and what we're NOT doing.
(defn- eval-lisp? [state now keystroke]
  (let [time-at-last-eval (:lisp-time state)]
    (and (< 1000 (- now time-at-last-eval)) ; wait at least a second
         (contains? {:right 1 :e 1 :d 1 :left 1 :a 1 :up 1 :down 1 :mouse-click 1}  keystroke))))

(defn update-state [state]
  (let [now           (now)
        keystroke     (get-keystroke-or-mouse)
        hero-location (char/move config/screen-size (get-in state [:hero :size]) (get-in state [:hero :position])
                                 (cond (= :right keystroke) 2 (= :e  keystroke)  2 (= :d keystroke) 2 (= :left keystroke) -2 (= :a keystroke) -2 :else 0)
                                 (cond (= :down  keystroke) 2 (= :up keystroke) -2 :else 0) )]

    ;; aim toward hero orbit (with offset for each one)
    (let [angle (:angle state)]
      
      ;; TODO this will go away
      (let [rnd-lisp-op     (if (eval-lisp? state now keystroke) (rand-nth lispm/operations) (:lisp-op state))
            new-lisp-script (if (eval-lisp? state now keystroke) (lispm/eval rnd-lisp-op) nil)
            new-lisp-result (if (eval-lisp? state now keystroke) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (lispm/eval-clojure new-lisp-script)) (:lisp-result state))
            new-lisp-time   (if (eval-lisp? state now keystroke) now (:lisp-time state))]
        
        ;; this is the new state
        {:color      (mod (+ (:color state) 0.7) 255)
         :angle      (+ (:angle state) ball/angle-speed)
         :bg         (:bg state)
         :hero       (-> (:hero state)
                         (assoc ,,, :position (:position hero-location))
                         (assoc ,,, :animation (char/get-animation-state keystroke)) )
         :balls (map #(assoc % :position (ball/aim-at (:position %) (ball/calculate-orbit-target angle (:hero state) %)))
                     (:balls state))

         :lisp-op     rnd-lisp-op
         :lisp-result new-lisp-result
         :lisp-time   new-lisp-time
         }) )))

(defn draw-state [state]  

  ;; clear the sketch by filling it with light-grey
  (quil/background config/background-color)
  (quil/image (:bg state) 0 0)

  ;; set default drawing colors
  (quil/stroke config/default-stroke-color) 
  (quil/fill   config/default-fill-color)
  
  ;;(js/console.log (str "hero: " (:position (:hero state))))

  ;; left aligned, hard-code the sizes in pixels, it's the only way to fly in 1983
  (quil/text-size 48)
  (quil/text-align :left)
  (quil/text (str (quil/hour) ":" (let [m (str (quil/minute))] (if (= 1 (count m)) (str "0" m) m))) 25 60)

  (quil/text-size 28)
  (quil/text-align :right)
  (quil/text (str (quil/month) "/" (quil/day)) 300 45)
  (quil/text-align :left)
  
  ;; draw hero
  (quil/image (char/get-image (:hero state))
           (get-in state [:hero :position :x])
           (get-in state [:hero :position :y])
           (get-in state [:hero :size :x])
           (get-in state [:hero :size :y])
           )

  (bubble/draw (:hero state) (char/select-speech-randomly))
  
  ;; draw magic lambda balls
  (quil/text-size 12)
  (dorun ;; drawing is I/O and is side-effect, force it to run
   (map #(quil/text "λ" (get-in % [:position :x]) (get-in % [:position :y]))
        (:balls state)))
   
  ;; debugging text
  ;(quil/text (:position (:hero state)) 10 300)
  )

;; ensure additions are reflected in sketch calls
(defonce sketch-opts
  {:host "mccarthy-animation"
   :size [(:x config/screen-size) (:y config/screen-size)]
   :setup setup ; setup function called only once, during sketch initialization.
   :update update-state ; update-state is called on each iteration before draw-state.
   :no-start true ; disable autostart
   :draw draw-state
   :title "McCarthy Animation"
   :middleware [quilm/fun-mode]})

;; cljs start
;; TODO think about how to fix this without macros or a real eval
#?(:cljs
   (quil/defsketch mccarthy-animation :host (:host sketch-opts) :size (:size sketch-opts) :setup (:setup sketch-opts) :update (:update sketch-opts) :no-start (:no-start sketch-opts) :draw (:draw sketch-opts) :title (:title sketch-opts) :middleware (:middleware sketch-opts) ))

;; clj application start
#?(:clj
   (defn -main [& args]
     (println "App running, look up and enjoy.")
     (if (or (= "debug" (first args)) (= "-debug" (first args)) (= "-d" (first args)))
       (do
         (println "starting nrepl on port 4006, kindly jack in")
         (future (nrepl/start-server :port 4006)))) 
     (quil/sketch
      :features [:exit-on-close]
      :host (:host sketch-opts) :size (:size sketch-opts) :setup (:setup sketch-opts) :update (:update sketch-opts) :no-start (:no-start sketch-opts) :draw (:draw sketch-opts) :title (:title sketch-opts) :middleware (:middleware sketch-opts) ) ))
