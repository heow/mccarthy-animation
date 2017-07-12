(ns mccarthy-animation.core
  (:require [original-lisp.core :as lisp]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [mccarthy-animation.character :as hero]
            [mccarthy-animation.lispm :as lispm]
            ))

(def screen-size {:x 320 :y 320})
(def sprite-size {:x 64  :y 64})

(defn say [list-of-symbols]
  (apply str (interpose " " list-of-symbols)))

(defn move-up [distance]
  (str "moving up " distance " ..."))

(defn move-hero [position x-delta y-delta]
  (let [proposed-x (+ (:x position) x-delta)
        proposed-y (+ (:y position) y-delta)]
    ;; keep him on-screen
    (if (hero/move-to? screen-size sprite-size {::hero/x proposed-x ::hero/y proposed-y})
      {:position {:x proposed-x :y proposed-y }}
      {:position position})
    ))

(defn setup []
  (q/frame-rate 30)   ; Set frame rate to 30 frames per second.
  (q/color-mode :hsb) ; Set color mode to HSB (HSV) instead of default RGB.
  
  ;; text
  (q/text-font (q/create-font "DejaVu Sans" 10 true))

  ;; setup function returns initial state. It contains
  ;; circle color and position.
  {:color 0
   :angle 0
   :bg (q/load-image "resources/background.png")
   :hero {:position {:x 120 :y 120}
          :image (q/load-image "resources/megaman.png")}
   :lisp-result ""
   :lisp-time 0
   })

(defn now [] (q/millis))

(defn get-keystroke-or-mouse []
  (cond (q/key-pressed?) (q/key-as-keyword)
        (q/mouse-pressed?) :mouse-click
        :else :none
        ))

;; Hack to determine when we want to evaluate some lisp, checks to see what we are
;; and what we're NOT doing.
(defn eval-lisp? [state now keystroke]
  (let [time-at-last-eval (:lisp-time state)]
    (and (< 1000 (- now time-at-last-eval)) ; wait at least a second
         (contains? {:right 1 :e 1 :d 1 :left 1 :a 1 :up 1 :down 1 :mouse-click 1}  keystroke))))

(defn update-state [state]
  (let [now           (now)
        keystroke     (get-keystroke-or-mouse)
        hero-location (move-hero (:position (:hero state))
                                 (cond (= :right keystroke) 2 (= :e  keystroke)  2 (= :d keystroke) 2 (= :left keystroke) -2 (= :a keystroke) -2 :else 0)
                                 (cond (= :down  keystroke) 2 (= :up keystroke) -2 :else 0) )]
    (let [rnd-lisp-op     (if (eval-lisp? state now keystroke) (rand-nth lispm/operations) (:lisp-op state))
          new-lisp-script (if (eval-lisp? state now keystroke) (lispm/eval rnd-lisp-op) nil)
          new-lisp-result (if (eval-lisp? state now keystroke) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (lispm/eval-clojure new-lisp-script)) (:lisp-result state))
          new-lisp-time   (if (eval-lisp? state now keystroke) now (:lisp-time state))]
      {:color      (mod (+ (:color state) 0.7) 255)
       :angle      (+ (:angle state) 0.01)
       :bg         (:bg state)
       :hero       (assoc (:hero state) :position (:position hero-location)) 
       :lisp-op     rnd-lisp-op
       :lisp-result new-lisp-result
       :lisp-time   new-lisp-time
       }))
  )

(defn draw-state [state]  
  ;; Clear the sketch by filling it with light-grey color.
  (q/background 240)

  (q/image (:bg state) 0 0)
  
  ;; set the fill color
  (q/fill (:color state) 255 255)

  ;;(js/console.log (str "hero: " (:position (:hero state))))

  (q/image (:image (:hero state))
           (:x (:position (:hero state)))
           (:y (:position (:hero state)))
           (:x sprite-size)
           (:y sprite-size)) ; draw hero
  
  ;; Calculate x and y coordinates of the circle.
  (let [angle (:angle state)
        x (* 150 (q/cos angle))
        y (* 150 (q/sin angle))]
    
    ; Move origin point to the center of the sketch.
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]      
      (q/ellipse x y 100 100) ) ; draw the circle
    )
  
  ;; draw the text?
  (q/text (str (:lisp-op state) (if (empty? (:lisp-op state)) nil " => ") (:lisp-result state)) 10 300)
  ;;(q/text (str "pos: " (:position (:hero state))) 10 300)
  )

(q/defsketch mccarthy-animation
  :host "mccarthy-animation"
  :size [(:x screen-size) (:y screen-size)]
  :setup setup ; setup function called only once, during sketch initialization.
  :update update-state ; update-state is called on each iteration before draw-state.
  :draw draw-state
  ;; This sketch uses functional-mode middleware.
  ;; Check quil wiki for more info about middlewares and particularly
  ;; fun-mode.
  :middleware [m/fun-mode])
