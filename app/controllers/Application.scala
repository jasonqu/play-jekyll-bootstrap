package controllers

import play.api._
import play.api.mvc._
import models.Post
import org.joda.time.DateTime
import util.markdown.PegDown._
import org.joda.time.format.DateTimeFormat

object Application extends Controller {
  import java.io.File
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  
  def index = Action {
    val postFiles = recursiveListFiles(new File("_content/_posts")).filterNot(_.isDirectory)
    val posts = postFiles.map { file =>
      Post(
        "title",
        "tagline",
        file.getName(),
        new DateTime(),
        "content")
    }
    Ok(views.html.index("")(posts))
  }

  def post(id: String) = Action {
    val postFiles = recursiveListFiles(new File("_content/_posts"))
      .filterNot(_.isDirectory)
      .filter(_.getName().equals(id))
      
    if (postFiles.isEmpty) {
      NotFound
    }
    
    val posts = Some(postFiles(0)).map { file =>
      val (metadata, content) = processMdFile(file)
      
      val date = try { DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(file.getName().substring(0, 10))
      } catch {case _ => new DateTime(file.lastModified()) }
      
      Post(
        metadata.getOrElse("title", "title"),
        metadata.getOrElse("tagline", "tagline"),
        file.getName(),
        date,
        content)
    }
    Ok(views.html.post(posts.get))
//    posts match {
//      Some(post) => Ok(views.html.post(post))
//      _ => NotFound
//    }
    
  }

}