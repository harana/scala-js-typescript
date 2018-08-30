/* TypeScript importer for Scala.js
 * Copyright 2013-2014 LAMP/EPFL
 * @author  Sébastien Doeraene
 */

package org.scalajs.tools.tsimporter

import java.io
import java.io.{Console => _, Reader => _, _}

import scala.collection.immutable.PagedSeq
import Trees._

import scala.util.parsing.input._
import parser.TSDefParser

/** Entry point for the TypeScript importer of Scala.js */
object Main {
  def main(args: Array[String]) {

    val files = new File("/Developer/harana/DefinitelyTyped/types").listFiles().sortBy(_.getName)

    var i = 0

    files.foreach { file =>
      if (file.isDirectory) {
        val inputFileName = file.getAbsolutePath + "/index.d.ts"
        val className = file.getName.replace("-", "").replace("react","").capitalize
        importTsFile(inputFileName, s"/Users/naden/Desktop/types/${className}.scala", file.getName) match {
          case Right(()) =>
            ()
          case Left(message) =>
            i += 1
            Console.err.println(inputFileName + "\n" + message)
        }
      }
    }

    println("Failed: " + i)
}

  def importTsFile(inputFileName: String, outputFileName: String, appName: String): Either[String, Unit] = {
    parseDefinitions(readerForFile(inputFileName)).map { definitions =>
      val stringWriter = new StringWriter()
      val output = new io.PrintWriter(new FileWriter(outputFileName))
      try {
        process(definitions, output, appName)
        println(stringWriter)
        Right(())
      } finally {
        output.close()
      }
    }
  }

  private def process(definitions: List[DeclTree], output: PrintWriter, appName: String) {
    new Importer(output)(definitions, appName)
  }

  private def parseDefinitions(reader: Reader[Char]): Either[String, List[DeclTree]] = {
    val parser = new TSDefParser
    parser.parseDefinitions(reader) match {
      case parser.Success(rawCode, _) =>
        Right(rawCode)

      case parser.NoSuccess(msg, next) =>
        Left(
            "Parse error at %s\n".format(next.pos.toString) +
            msg + "\n" +
            next.pos.longString)
    }
  }

  /** Builds a [[scala.util.parsing.input.PagedSeqReader]] for a file
   *
   *  @param fileName name of the file to be read
   */
  private def readerForFile(fileName: String) = {
    new PagedSeqReader(PagedSeq.fromReader(
        new BufferedReader(new FileReader(fileName))))
  }
}
