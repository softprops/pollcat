$ = jQuery
$ ->

  if not 'console' in window
    window.console =
      log: (e) -> return

  questions = ($ '#questions')

  renderQuestion = (q) ->
    id = q.id.split(':').pop()
    "<li class='q' id='q-#{id}' data-votes='#{q.votes}'>
      <div class='txt'>#{q.text}</div>
      <div class='vote'>
        <div>
          <a class='y' href='#'>+</a>
          <a class='n' href='#'>-</a>
        </div>
        <div>
          <a class='d' href='#'>x</a>
        </div>
      </div>
    </li>"

  questionAsked = (q) ->
    questions.prepend renderQuestion(q)
    return

  upvote = (id) ->
    console.log "up voting #{id}"
    return

  downvote = (id) ->
    console.log "down voting #{id}"
    return

  process = (m) ->
    [action, id, data] = m.split ':'
    if action is 'up' then console.log 'data voted for'
    else if action is 'down' then console.log 'data voted against'
    else if action is 'ask'
      console.log 'question asked'
      questionAsked
        id: id,
        text: data,
        votes: 0
    else
      console.log "malformed message #{m}"

  $.get "/polls", name: 'talk', (qs) ->
    alert 'got questions'
    console.log qs
    (questionAsked(q) for q in qs)
    return

  $(".y").live 'click', (e) ->
    e.preventDefault()
    self = $(@)
    q = self.parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2), v: 'up'), (e) ->
      upvote(q) if e.status is '200'
      return
    false

  $(".n").live 'click', (e) ->
    e.preventDefault()
    q = $(@).parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2),  v: 'down'), (e) ->
      downvote(q) if e.status is '200'
      return
    false

  $(".d").live 'click', (e) ->
    e.preventDefault()
    self = $(@)
    q = self.parent().parent().parent().attr("id")
    $.post '/questions', (poll: 'talk', q: q.substring(2)), (e) ->
      ($("#" + q).fadeOut 'fast', (e) ->
        self.remove()
        return) if e.status is '200'
      return
    false

  $(".q").live "mouseover mouseout", (e) ->
    if e.type is "mouseover" then $(".vote", @).show()
    else $(".vote", @).hide()

  $("#ask").submit (e) ->
    e.preventDefault()
    $.post "/polls", $(this).serialize(), (d) ->
      $("#q").val("")
      false

  $("#login").live 'click', (e) -> $(@).html "..."

  if window.WebSocket || window.MozWebSocket
    uri = "ws://#{window.location.host}/cat/talk"
    cat = if window.WebSocket then new WebSocket(uri) else new MozWebSocket(uri)
    cat.onmessage = (m) ->
      process m.data
      return
    if 'onbeforeunload' in window then window.onbeforeunload (e) -> cat.close()
    return
  else
    alert 'Your browser is inferior and thus does not support the html5 standard for websockets'

  return