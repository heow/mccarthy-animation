(ns mccarthy-animation.core
  (:require [original-lisp.core :as lisp]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [cljs.js :refer [empty-state eval js-eval]]
            ))

(def screen-size {:x 320 :y 320})
(def sprite-size {:x 64  :y 64})

(defn say [list-of-symbols]
  (apply str (interpose " " list-of-symbols)))


(defn move-up [distance]
  (str "moving up " distance " ..."))

(defn eeval [l]
  (eval (empty-state)
        l
        {:eval       js-eval
         :source-map true
         ;; :ns         (find-ns 'mccarthy-animation.core) ; why does this not work?
         :context    :expr}
        (fn [result] result)) )

(def lisp-env
  '((a 1)
    (b 2)
    (c 3)
    (d 4)
    (f (lambda (x) (cons 'a x)))
    (y ((a b) (c d)))
    (saying1 '(hello there cruel world))
    (saying2 '(oh no not again))
    (get (lambda (x) (car (cons x '()))))
    (speak   (lambda () (cons 'mccarthy-animation.core.say (cons saying1 '()))))
    (move-up (lambda () (cons 'mccarthy-animation.core.move-up '(10))))
    ))

(def lisp-ops ['(speak)
               '(move-up)
               ])

(defn move-hero [position x-delta y-delta]
  (let [x-boundry (- (:x screen-size) (:x sprite-size))
        y-boundry (:y screen-size)
        proposed-x (+ (:x position) x-delta)
        proposed-y (+ (:y position) y-delta)]
    ;; keep him on-screen
    {:type :new-position
     :position {:x (cond (< proposed-x 0) 0
                         (> proposed-x (- (:x screen-size) (:x sprite-size))) (- (:x screen-size) (:x sprite-size))
                         :else proposed-x)
                :y (cond (< proposed-y 0) 0
                         (> proposed-y (- (:y screen-size) (:y sprite-size))) (- (:y screen-size) (:y sprite-size))
                         :else proposed-y)
                }}))

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

;; Sort of a hack to determine when we want to evaluate some lisp, checks to see what we are
;; and what we're NOT doing.
(defn eval-lisp? [state]
  (let [time-at-last-eval (:lisp-time state)]
    (and (< 1000 (- (q/millis) time-at-last-eval)) ; wait a second
       (or (and (q/key-pressed?) (not (contains? {:right 1 :e 1 :d 1 :left 1 :a 1 :up 1 :down 1}  (q/key-as-keyword))) ) ;; TODO fix hack
           (q/mouse-pressed?) ))))

(defn update-state [state]
  (let [hero-location (move-hero (:position (:hero state))
                                 (if (not (q/key-pressed?)) 0 (cond (= :right (q/key-as-keyword)) 2 (= :e     (q/key-as-keyword)) 2 (= :d     (q/key-as-keyword)) 2 (= :left  (q/key-as-keyword)) -2 (= :a     (q/key-as-keyword)) -2 :else 0))
                                 (if (not (q/key-pressed?)) 0 (cond (= :down  (q/key-as-keyword)) 2 (= :up   (q/key-as-keyword)) -2 :else 0))
                                 )]
    ;(js/console.log "hero location" (str (:position hero-location)))
    (let [new-lisp-op     (if (eval-lisp? state) (rand-nth lisp-ops) (:lisp-op state))
          new-lisp-script (if (eval-lisp? state) (lisp/l-eval new-lisp-op lisp-env) (:lisp-script state))
          new-lisp-result (if (eval-lisp? state) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (:value (eeval new-lisp-script)))  (:lisp-result state))
          new-lisp-time   (if (eval-lisp? state) (q/millis) (:lisp-time state))]
      {:color      (mod (+ (:color state) 0.7) 255)
       :angle      (+ (:angle state) 0.01)
       :bg         (:bg state)
       :hero       (assoc (:hero state) :position (:position hero-location)) 
       :lisp-op     new-lisp-op
       :lisp-script new-lisp-script
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

  ;;(if (q/key-pressed?) (js/console.log "key " (q/raw-key)))
  ;;(if (not (empty? (:lisp-result state))) (js/console.log (str "l: " (:lisp-result state))))
  ;;(if (not (empty? (:lisp-result state))) (js/console.log (str "l: " ((:lisp-result state)))))
  ;;(js/console.log (type (say (quote (hello there cruel world)))))
  ;;(if (not (empty? (:lisp-result state))) (js/console.log (js/eval (:lisp-result state))))
  ;;(if (not (empty? (:lisp-result state))) (js/console.log (first (:lisp-result state))))
  ;;(if (not (empty? (:lisp-result state))) (js/console.log eeval (say '(hello there))))
  ;;(if (q/key-pressed?) (js/console.log "ns " (find-ns 'mccarthy-animation.core) ))
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
  ;(q/text (str (:lisp-op state) (if (empty? (:lisp-op state)) nil " => ") (:value (eeval (:lisp-result state)))) 10 300)
  (q/text (str (:lisp-op state) (if (empty? (:lisp-op state)) nil " => ") (:lisp-result state)) 10 300)
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



