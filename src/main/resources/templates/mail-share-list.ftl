<html>
<h3>Hi ${shareList.forUser.username}</h3>
<p>
You have been invited by user '${shareList.fromUser.username}' for his shopping list '${shareList.sharedList.name}'. If you want to accept this
invitation, please follow the link below.
</p>
<p>
<a href="${shareListLink}/share/${shareListToken}">Please click here</a>
</p>
<p>
Sincerely,
</p>
<p>
Your Shopping List Team.
</p>
</html>