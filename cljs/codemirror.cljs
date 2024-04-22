(require '[clojure.string :as str])
(declare cm)

(defn eval-me []
  (js/scittle.core.eval_string (-> cm .-state .-doc .toString)))

(def extension
  (.of js/cv.keymap
       (clj->js [{:key "Mod-Enter"
                  :run (fn []
                         (eval-me))}
                 #_{:key (str modifier "-Enter")
                    :shift (partial eval-top-level on-result)
                    :run (partial eval-at-cursor on-result)}])))
(def cm
  (let [doc (str/trim "
(require '[reagent.core :as r]
         '[reagent.dom :as rdom]
         '[re-frame.core :as rf])

(rf/reg-event-fx ::click (fn [{:keys [db]} _] {:db (update db :clicks (fnil inc 0))}))
(rf/reg-sub ::clicks (fn [db] (:clicks db)))

(defn my-component []
  (let [clicks (rf/subscribe [::clicks])]
    [:div
      [:p \"Clicks: \" @clicks]
      [:p [:button {:on-click #(rf/dispatch [::click])}
            \"Click me!\"]]]))

(rdom/render [my-component] (.getElementById js/document \"reagent\"))
")]
    (js/cm.EditorView. #js {:doc doc
                            :extensions #js [js/cm.basicSetup, (js/lc.clojure), (.highest js/cs.Prec extension)]
                            :parent (js/document.querySelector "#app")
                            #_#_:dispatch (fn [tr] (-> cm (.update #js [tr])) (eval-me))
                            })))
(set! (.-eval_me js/globalThis) eval-me)
(set! (.-cm_instance js/globalThis) cm)

(defn linux? []
  (some? (re-find #"(Linux)|(X11)" js/navigator.userAgent)))

(defn mac? []
  (and (not (linux?))
       (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform))))

(let [elt (js/document.getElementById "evalMe")
      txt (.-innerText elt)
      mod-symbol (if (mac?)
                   "⌘"
                   "⌃")
      txt (str txt " " mod-symbol"-⏎")]
  (set! (.-innerHTML elt) txt))

(eval-me)
