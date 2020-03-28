/*
 * GameBox
 * Copyright (C) 2019  Niklas Eicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.module.cloud;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.exceptions.module.GameBoxCloudException;
import me.nikl.gamebox.module.data.CloudModuleData;
import me.nikl.gamebox.module.data.CloudModuleDataWithVersion;
import com.google.gson.Gson;
import me.nikl.gamebox.module.data.VersionedCloudModule;
import me.nikl.gamebox.utility.GameBoxGsonBuilder;
import me.nikl.gamebox.utility.versioning.SemanticVersion;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CloudFacade {
    private static final String API_BASE_URL = "https://api.gamebox.nikl.me/";
    private static final Gson GSON = GameBoxGsonBuilder.build();
    private static SSLSocketFactory factoryTrustingLetsEncrypt;

    static {
        try (InputStream caInput = GameBox.getProvidingPlugin(GameBox.class).getResource("dst-root-ca-x3.pem")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(caInput);

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            factoryTrustingLetsEncrypt = context.getSocketFactory();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            e.printStackTrace();
        }
    }

    public ApiResponse<CloudModuleData[]> getCloudModuleData() {
        ApiResponse<InputStream> stream = this.openStream("modules");
        if (stream.getError() != null) {
            return new ApiResponse<>(null, stream.getError());
        }
        CloudModuleData[] modulesData = GSON.fromJson(new InputStreamReader(stream.getData()), CloudModuleData[].class);
        return new ApiResponse<>(modulesData, null);
    }

    public ApiResponse<CloudModuleDataWithVersion[]> getCloudModuleDataWithVersion(String moduleId) {
        ApiResponse<InputStream> stream = this.openStream(String.format("module/%s", moduleId));
        if (stream.getError() != null) {
            return new ApiResponse<>(null, stream.getError());
        }
        CloudModuleDataWithVersion[] modulesData = GSON.fromJson(new InputStreamReader(stream.getData()), CloudModuleDataWithVersion[].class);
        return new ApiResponse<>(modulesData, null);
    }

    public ApiResponse<VersionedCloudModule> getVersionedCloudModuleData(String moduleId, SemanticVersion version) {
        ApiResponse<InputStream> stream = this.openStream(String.format("module/%s/%s", moduleId, version.toString()));
        if (stream.getError() != null) {
            return new ApiResponse<>(null, stream.getError());
        }
        VersionedCloudModule modulesData = GSON.fromJson(new InputStreamReader(stream.getData()), VersionedCloudModule.class);
        return new ApiResponse<>(modulesData, null);
    }

    private ApiResponse<InputStream> openStream(String path) {
        try {
            URL url = new URL(API_BASE_URL + path);
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(factoryTrustingLetsEncrypt);
            InputStream in = urlConnection.getInputStream();
            return new ApiResponse<>(in, null);
        } catch (UnknownHostException e) {
            return new ApiResponse<>(null, new GameBoxCloudException("Connection problem to the cloud. Please make sure that you are connected to the internet.", e));
        } catch (IOException e) {
            return new ApiResponse<>(null, new GameBoxCloudException(e));
        }
    }
}
