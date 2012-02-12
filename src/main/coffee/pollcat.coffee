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
          <a class='a' href='#'>!</a>
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
    if action is 'up' then upvote(id)
    else if action is 'down' then downvote(id)
    else if action is 'ask'
      questionAsked
        id: id,
        text: data,
        votes: 0
    else
      console.log "malformed message #{m}"

  # upvote
  $(".y").live 'click', (e) ->
    e.preventDefault()
    self = $(@)
    q = self.parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2), v: 'up'), (e) ->
      upvote(q) if e.status is 200
      return
    false

  # downvote
  $(".n").live 'click', (e) ->
    e.preventDefault()
    q = $(@).parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2),  v: 'down'), (e) ->
      downvote(q) if e.status is 200
      return
    false

  # delete
  $(".d").live 'click', (e) ->
    e.preventDefault()
    self = $(@)
    q = self.parent().parent().parent().attr("id")
    $.post '/questions', (poll: 'talk', q: q.substring(2)), (e) ->
      ($("#" + q).fadeOut 'fast', (e) ->
        self.remove()
        return) if e.status is 200
      return
    false

  # ask
  $(".a").live "click", (e) ->
    e.preventDefault()
    q = $(@).parent().parent().parent().attr("id")
    $("#asking").text($("#"+q).find(".txt").text())
    false

  # voting options
  $(".q").live "mouseover mouseout", (e) ->
    if e.type is "mouseover" then $(".vote", @).show()
    else $(".vote", @).hide()

  # ask
  $("#ask").submit (e) ->
    e.preventDefault()
    if $.trim($("#q").val()).length > 0
      $.post "/polls", $(this).serialize(), (d) ->
        $("#q").val("")
    false


  # get all of a poll's questions
  $.get("/polls",
    name: 'talk',
    (qs) => questionAsked(q) for q in qs
  ).error (a, e) -> console.log e

  $("#login").live 'click', (e) -> $(@).html "Redirecting to Meetup"

  # pushed questions and votes
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