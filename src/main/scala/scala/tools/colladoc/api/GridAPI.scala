/*
 * Copyright (c) 2011, Sergey Ignatov. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scala.tools.colladoc
package api

import model.mapper.User
import net.liftweb.http._
import rest.RestHelper
import net.liftweb.mapper.{OrderBy, Descending, Ascending}

/**
 * Grid API helper.
 * @author Sergey Ignatov
 */
object GridAPI extends RestHelper {
  serve {
    case "grid" :: "users" :: _ Get _ =>
      if (User.superUser_?) {
        for {
          Spage <- S.param("page") ?~ "page parameter missing" ~> 400
          Srows <- S.param("rows") ?~ "rows parameter missing" ~> 400
          Ssord <- S.param("sord") ?~ "sord parameter missing" ~> 400
          Ssidx <- S.param("sidx") ?~ "sidx parameter missing" ~> 400
        } yield {
          var page = Spage.toInt
          val rows = Srows.toInt
          val count = User.count
          val totalPages = if (count > 0) math.ceil(count / rows).toInt + 1 else 0
          if (page > totalPages)
            page = totalPages

          val order = Ssord match {
            case "asc" => Ascending
            case "desc" => Descending
            case _ => Descending
          }

          val sortingCol = Ssidx match {
            case "username" => OrderBy(User.userName, order)
            case "email" => OrderBy(User.email, order)
            case "openid" => OrderBy(User.openId, order)
            case "superuser" => OrderBy(User.superUser, order)
            case _ => OrderBy(User.userName, order)
          }

          val users = User.findAll(sortingCol).slice((page - 1)* rows, page * rows).map(_.toGridRow)

          <rows>
            <page>
              {page}
            </page>
            <total>
              {totalPages}
            </total>
            <records>
              {count}
            </records>
            {users}
          </rows>
        }
      } else
        NotFoundResponse()
  }
}