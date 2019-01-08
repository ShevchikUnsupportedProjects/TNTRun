/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package tntrun;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams
{
  public static int pipeStreams(InputStream in, OutputStream out)
  {
    try
    {
      byte[] b = new byte[8192];
      
      int total = 0;
      for (;;)
      {
        try
        {
          int read = in.read(b);
          if (read == -1) {
            break;
          }
          out.write(b, 0, read);
        }
        catch (IOException e) {}
        int read = 0;
        total += read;
      }
      out.flush();
      
      return total;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return -1;
  }
  
  public static void pipeStreamsActive(final InputStream in, final OutputStream out)
  {
    Thread thread = new Thread("Active Pipe Thread")
    {
      public void run()
      {
        byte[] b = new byte[8192];
        try
        {
          for (;;)
          {
            System.out.println("Proxy pipeStreamsActive reading");
            int count = in.read(b);
            System.out.println("Proxy pipeStreamsActive donereading");
            if (count == -1) {
              break;
            }
            System.out.println("Proxy pipeStreamsActive writing");
            out.write(b, 0, count);
            System.out.println("Proxy pipeStreamsActive donewriting");
            out.flush();
          }
          return;
        }
        catch (IOException localIOException1)
        {
          try
          {
            out.close();
            in.close();
          }
          catch (IOException e)
          {
            System.out.println("Proxy pipeStreamsActive:");
            e.printStackTrace();
          }
        }
      }
    };
    thread.start();
  }
  
  public static byte[] toByteArray(InputStream s)
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    
    pipeStreams(s, os);
    
    return os.toByteArray();
  }
  
  public static String toString(InputStream s)
  {
    return new String(toByteArray(s));
  }
  
  public static void close(Closeable s)
  {
    try
    {
      s.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
