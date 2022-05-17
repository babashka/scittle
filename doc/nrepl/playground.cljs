(ns playground)

(+ 1 2 3)

(->
 (js/document.getElementsByTagName "body")
 first
 (.append
  (doto (js/document.createElement "p")
    (.append
     (js/document.createTextNode "there")))))

(defn foo [])

(js/alert "Isn't this cool? :)")
