package pw.yumc.YumCore.kit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * ZIP操作类
 *
 * @since 2016年7月19日 上午10:24:06
 * @author 喵♂呜
 */
public class ZipKit {
    /**
     * 获取文件真实名称
     *
     * @param name
     *            名称
     * @return
     */
    public static String getRealName(final String name) {
        return new File(name).getName();
    }

    /**
     * @param zipFile
     *            zip文件
     * @param destPath
     *            解压目录
     * @throws ZipException
     *             ZIP操作异常
     * @throws IOException
     *             IO异常
     */
    public static void unzip(final File zipFile, final File destPath) throws ZipException, IOException {
        unzip(zipFile, destPath, null);
    }

    /**
     * @param zipFile
     *            zip文件
     * @param destPath
     *            解压目录
     * @param ext
     *            解压后缀
     * @throws ZipException
     *             ZIP操作异常
     * @throws IOException
     *             IO异常
     */
    public static void unzip(final File zipFile, final File destPath, final String ext) throws ZipException, IOException {
        final ZipFile zipObj = new ZipFile(zipFile);
        final Enumeration<? extends ZipEntry> e = zipObj.entries();
        while (e.hasMoreElements()) {
            final ZipEntry entry = e.nextElement();
            final File destinationFilePath = new File(destPath, getRealName(entry.getName()));
            if (entry.isDirectory() || (ext != null && !destinationFilePath.getName().endsWith(ext))) {
                continue;
            }
            destinationFilePath.getParentFile().mkdirs();
            Files.copy(zipObj.getInputStream(entry), destinationFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        zipObj.close();
    }
}
