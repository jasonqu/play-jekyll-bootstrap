package util.xml

import models.Post
import play.api.Play.current

object Xml {
  def atomPost(post: Post) = {
    <entry>
      <title>{ post.title }</title>
      <link href="/post/{post.name}"/>
      <updated>{ post.date }</updated>
      <id>{ current.configuration.getString("production_url") }/post/{ post.name }</id>
      <content type="html">{ post.html }</content>
    </entry>
  }

  def atom(posts: Array[models.Post]) = {
    <?xml version="1.0" encoding="utf-8"?>
    <feed xmlns="http://www.w3.org/2005/Atom">
      <title>{ current.configuration.getString("siteTitle") }</title>
      <link href={ current.configuration.getString("production_url") + "/atom.xml" } rel="self"/>
      <link href={ current.configuration.getString("production_url") + ""}/>
      <updated>site.time</updated>
      <id>{ current.configuration.getString("production_url") }</id>
      <author>
        <name>{ current.configuration.getString("author.name") }</name>
        <email>{ current.configuration.getString("author.email") }</email>
      </author>
      {for (post <- posts) yield {atomPost(post)}}
    </feed>
  }

  def rssPost(post: Post) = {
    <item>
      <title>{ post.title }</title>
      <description>{ post.html }</description>
      <link>{ current.configuration.getString("production_url") + "/post/" + post.name }</link>
      <guid>{ current.configuration.getString("production_url") + "/post/" + post.name }</guid>
      <pubDate>{ post.date }</pubDate>
    </item>
  }

  def rss(posts: Array[models.Post]) = {
    <rss version="2.0">
      <channel>
        <title>{ current.configuration.getString("siteTitle") }</title>
        <description>{ current.configuration.getString("siteTitle") } - { current.configuration.getString("author.name") }</description>
        <link>{ current.configuration.getString("production_url") + "" }/rss.xml</link>
        <link>{ current.configuration.getString("production_url") + "" }</link>
        <lastBuildDate>site.time</lastBuildDate>
        <pubDate>site.time</pubDate>
        <ttl>1800</ttl>
        { for (post <- posts) yield { rssPost(post) } }
      </channel>
    </rss>
  }

}