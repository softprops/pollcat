$ = jQuery
$ ->
  $.get "/poll", (qs) ->
    console.log "questions #{qs}"

  queue = []
  enqueue = (e) -> queue.push(e)
  dequeue = () -> queue.shift()
  pollq = () ->
    console.log 'poll'
    e = dequeue()
    if e then process(e)

  if window.WebSocket || window.MozWebSocket
    uri = "ws://#{window.location.host}/cat/talk"
    cat = if window.WebSocket then new WebSocket(uri) else new MozWebSocket(uri)
    cat.onmessage = (m) ->
      console.log "msg #{m.data}"
      return
    cat.onopen = () ->
      console.log 'open'
      return
    return
  else
    alert 'Your browser is inferior and thus does not the html5 standard for websockets'

  setInterval(pollq, 200)
  return