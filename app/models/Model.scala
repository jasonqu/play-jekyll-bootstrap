package models

import org.joda.time._

case class Post(
  title: String,
  tagline: String,
  name: String, // filename
  date: DateTime,
  html: String) {
}