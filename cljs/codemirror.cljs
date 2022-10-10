(require '[clojure.string :as str])
(declare cm)

(defn eval-me []
  (js/scittle.core.eval_string (-> cm .-state .-doc .toString)))

(def extension
  (.of js/cv.keymap
       (clj->js [{:key "Mod-Enter"
                  :run (fn []
                         (prn :hoeooo)
                         (eval-me))}
                 #_{:key (str modifier "-Enter")
                    :shift (partial eval-top-level on-result)
                    :run (partial eval-at-cursor on-result)}])))
(def cm
  (let [doc (str/trim "
(require '[reagent.core :as r]
         '[reagent.dom :as rdom])

(defonce state (r/atom {:clicks 0}))

(defn my-component []
  [:div
    [:p \"Clicks: \" (:clicks @state)]
    [:p [:button {:on-click #(swap! state update :clicks inc)}
          \"Click me!\"]]])

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
