(ns snake-game.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]))

(enable-console-print!)

(println "Reloading the app !")

(def snake { :position [0,0], :points [[1,0], [2,0]] })

(def app-db  (reagent/atom {}))

(rf/reg-event-db              ;; sets up initial application state
  :initialize
  (fn [_ _]
    {:board-size [5,10]
     :snake snake
     :game-running? true}))

(rf/reg-sub
  :board-size
  (fn [db _]
    (:board-size db)))

(rf/reg-sub
  :snake
  (fn [db _]
    (:snake db)))

(rf/reg-sub
  :snake-pos
  (fn [db _]
    (:position (:snake db))))

(def code->key
  "map from a character code (read from events with event.which)"
  {37 "left"
   38 "up"
   39 "right"
   40 "down"})

(def cell-style {:style {:display "table-cell" :position "relative"}})
(def base-tile-style {:display "block" :height 75 :width 75 })
(def background-tile-style (merge base-tile-style {:position "relative" :z-index -1}))
(def worm-tile-style (merge base-tile-style {:position "absolute" :z-index 1 :top 0 :left 0}))

(defn single-row [pos_x size_y snake-position]
  [:div {:key pos_x :style {:display "table-row"}}
   (for [pos_y (range size_y)]
     [:div (merge {:key [pos_x, pos_y]} cell-style)
       [:img {:style background-tile-style :src "grass.png" }]
       (if (= snake-position [pos_x, pos_y])
         [:img {:style worm-tile-style :src "snake_start.png" }])
      ])])

(defn render-rows [board-size]
  [:div
   (for [pos_x (range (first board-size))]
     (single-row pos_x (last board-size) @(rf/subscribe [:snake-pos])))])

(defn ui
  []
  [:div
   [:h1 "A ClojureScript snake-game using reframe and reagent"]
   (render-rows @(rf/subscribe [:board-size]))
   ])

(defn keydown [e]
  (if (contains? [37 38 39 40] (.-keyCode e))
    (rf/dispatch [:keypressed e])
    (.preventDefault e)))

(defn ^:export run
  []
  (set! (.-onkeydown js/document) keydown)
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))

(run)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ; (swap! app-state update-in [:__figwheel_counter] inc)
  )
