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
    {:board-size [5,5]
     :snake snake
     :game-running? true}))

(rf/reg-sub
  :board-size
  (fn [db _]
    (:board-size db)))

(def cell-style {:style {:display "table-cell"
                                 :height 50
                                 :width 50
                                 :color "white"
                                 :border "2px solid black"}})

(defn single-row [pos_x size_y]
  [:div {:key pos_x :style {:display "table-row"}}
   (for [pos_y (range size_y)]
     [:div (merge {:key [pos_x, pos_y]} cell-style)])])

(defn render-rows [board-size]
  [:div
   (for [pos_x (range (first board-size))]
     (single-row pos_x (last board-size)))])

(defn ui
  []
  [:div
   [:h1 "A ClojureScript snake-game using reframe and reagent"]
   (render-rows @(rf/subscribe [:board-size]))
   ])

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))

(run)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ; (swap! app-state update-in [:__figwheel_counter] inc)
  )
