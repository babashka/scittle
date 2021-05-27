(ns bookmarklet
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn append-tag [tag {:keys [body onload onerror] :as attributes}]
  (str "var s=document.createElement('" (name tag) "');"
       (clojure.string/join ";" (map (fn [[k v]] (str "s.setAttribute('" (name k) "','" (name v) "')")) (dissoc attributes :body :onload :onerror)))
       (when body
         (str ";s.innerText=" body))
       (when onload
         (str ";s.onload=" onload))
       (when onerror
         (str ";s.onerror=" onerror))
       ";document.body.appendChild(s);"))

(defn pr-code [code-str]
  (let [s (pr-str (str "#_CODE_" code-str "#_CODE_"))]
    (str "\"" (subs s 1 (dec (count s))) "\"")))

(defn read-code [code]
  (read-string (str "\"" code "\"")))

(defn extract-code [code-str]
  (second (re-find #"#_CODE_(.+)#_CODE_" code-str)))

(defn bookmarklet-href [code-str]
  (str "javascript:(function(){"
       "var runCode = function() {
          try {
            scittle.core.eval_string(" (pr-code code-str) ")
          } catch (error) {
            console.log('Error in code', error);
            alert('Error running code, see console')
          }
        };"
       "if(typeof scittle === 'undefined'){"
       (append-tag :script {:src "https://borkdude.github.io/scittle/js/scittle.js"
                            :onerror "function(){alert('Error loading ' + this.src)}"
                            :onload (str "runCode")
                            })
       "} else {
         runCode() }"
       "})();"))


(defn workspace []
  (let [value (str "; This is the code of your bookmarklet\n"
                   (pr-str '(js/alert "Hello")))
        *code (r/atom value)
        bookmark-name "Bookmark"
        *bookmark-name (r/atom bookmark-name)]

   [:div
     [:input {:type "text"
              :placeholder bookmark-name
              :on-change (fn [e]
                           (let [v (.. e -target -value)]
                             (reset! *bookmark-name
                                     (if (clojure.string/blank? v)
                                         bookmark-name
                                         v))))}]
     [:br]
     [:textarea {:rows 10 :cols 80
                 :on-drop (fn [e]
                            (let [bookmarklet (js/decodeURIComponent (.. e -dataTransfer (getData "text")))
                                  cljs-snippet (some-> (extract-code bookmarklet)
                                                       read-code)
                                  new-code (if cljs-snippet
                                         (str "; Extracted snippet\n" cljs-snippet)
                                         (str "; Failed to extract snippet\n" bookmarklet))]
                              (js/console.log "Dropped" bookmarklet)
                              (set! (.. e -target -value) new-code)
                              (reset! *code new-code)
                              (.preventDefault e)))
                 :on-change (fn [e] (reset! *code (.. e -target -value)))}
                value]
     [:br]
     [:br]
     "Click the link below"[:br]
     "or"[:br]
     "Drag the link to the bookmarks bar" [:br]
     [(fn []
        (js/console.log "Loaded", @*code)
        [(fn [] [:a {:href (bookmarklet-href @*code)} @*bookmark-name])])
      *code]]))

(rdom/render [workspace] (.getElementById js/document "app"))
