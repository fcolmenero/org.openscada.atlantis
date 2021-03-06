/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassid.de)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.sec.authz.signature;

import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.1
 */
public class X509CA
{

    private final static Logger logger = LoggerFactory.getLogger ( X509CA.class );

    private volatile X509Certificate[] certificates;

    private volatile X509CRL[] crls;

    private final String certificateUrl;

    private final Collection<String> crlUrls;

    private final CertificateFactory certificateFactory;

    public X509CA ( final CertificateFactory cf, final String certificateUrl, final Collection<String> crlUrls )
    {
        this.certificateFactory = cf;
        this.certificateUrl = certificateUrl;
        this.crlUrls = crlUrls != null ? new ArrayList<String> ( crlUrls ) : null;

        this.certificates = new X509Certificate[0];
        this.crls = new X509CRL[0];
    }

    public void load () throws Exception
    {
        final Collection<X509Certificate> certificates = loadCert ( this.certificateUrl );
        final Collection<X509CRL> crls = loadCrl ( this.crlUrls );

        this.certificates = certificates.toArray ( new X509Certificate[certificates.size ()] );
        this.crls = crls.toArray ( new X509CRL[crls.size ()] );

    }

    @SuppressWarnings ( "unchecked" )
    private Collection<X509CRL> loadCrl ( final Collection<String> crls ) throws Exception
    {
        if ( crls == null || crls.isEmpty () )
        {
            return Collections.emptyList ();
        }

        final Collection<X509CRL> result = new LinkedList<X509CRL> ();

        for ( final String crl : crls )
        {
            logger.info ( "Loading CA CRL from : {}", crl );

            final InputStream stream = new URL ( crl ).openStream ();
            try
            {
                final Collection<X509CRL> crlData = (Collection<X509CRL>)this.certificateFactory.generateCRLs ( stream );
                logger.debug ( "Loaded {} entries", crlData );
                result.addAll ( crlData );
            }
            finally
            {
                stream.close ();
            }
        }

        logger.info ( "Finished loading CRLs - {} found", result.size () );

        return result;
    }

    @SuppressWarnings ( "unchecked" )
    private Collection<X509Certificate> loadCert ( final String value ) throws Exception
    {
        logger.info ( "Loading CA cert from : {}", value );

        final InputStream stream = new URL ( value ).openStream ();
        try
        {
            final Collection<X509Certificate> result = (Collection<X509Certificate>)this.certificateFactory.generateCertificates ( stream );

            logger.info ( "Finished loading CA certs - {} found", result.size () );

            return result;
        }
        finally
        {
            stream.close ();
        }
    }

    public X509Certificate[] getCertificates ()
    {
        return this.certificates;
    }

    public X509CRL[] getCrls ()
    {
        return this.crls;
    }

    public boolean isRevoked ( final X509Certificate cert )
    {
        for ( final X509CRL crl : this.crls )
        {
            if ( crl.isRevoked ( cert ) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean isValid ()
    {
        for ( final X509Certificate cert : this.certificates )
        {
            try
            {
                cert.checkValidity ();
                return true;
            }
            catch ( final Exception e )
            {
            }

        }
        return false;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[CA - cert: {}, crls: {}]", this.certificateUrl, this.crlUrls );
    }
}
