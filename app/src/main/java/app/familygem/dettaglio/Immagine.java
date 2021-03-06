package app.familygem.dettaglio;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Media;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.Source;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import app.familygem.Chiesa;
import app.familygem.Dettaglio;
import app.familygem.Galleria;
import app.familygem.Globale;
import app.familygem.Lavagna;
import app.familygem.Ponte;
import app.familygem.R;
import app.familygem.U;
import static app.familygem.Globale.gc;

public class Immagine extends Dettaglio {

	Media m = (Media) Ponte.ricevi( "oggetto" );

	@Override
	public void impagina() {
		if( m != null ) {
			if( m.getId() != null ) {
				setTitle( R.string.shared_media );
				vistaId.setText( m.getId() );    // 'O1' solo per Multimedia Records
			} else {
				setTitle( R.string.media );
				vistaId.setText( "OBJE" );
			}
			oggetto = m;
			immaginona( m );
			metti( getString(R.string.title), "Title" );
			metti( getString(R.string.type), "Type", false, false );	// _type
			if(Globale.preferenze.esperto) metti( getString(R.string.file), "File" );	// Angelina Guadagnoli.jpg
				// dovrebbe essere max 259 characters
			metti( getString(R.string.format), "Format", Globale.preferenze.esperto, false );	// jpeg
			metti( getString(R.string.primary), "Primary" );	// _prim
			metti( getString(R.string.scrapbook), "Scrapbook", false, false );	// _scbk the multimedia object should be in the scrapbook
			metti( getString(R.string.slideshow), "SlideShow", false, false );	//
			metti( getString(R.string.blob), "Blob", false, true );
			//s.l( m.getFileTag() );	// FILE o _FILE
			mettiEstensioni( m );
			U.mettiNote( box, m, true );
			U.cambiamenti( box, m.getChange() );

			// Lista delle persone e delle fonti in cui è usato il media
			List<Person> listaPersone = new ArrayList<>();
			for( Person p : gc.getPeople() ) {
				for( Media med : p.getAllMedia( gc ) )    // tutti i media in questo individuo
					if( med.equals( m ) )
						listaPersone.add( p );
			}
			List<Source> listaFonti = new ArrayList<>();
			for( Source s : gc.getSources() ) {
				for( Media med : s.getAllMedia( gc ) )    // tutti i media in questa fonte
					if( med.equals( m ) )
						listaFonti.add( s );
			}
			List<Family> listaFamiglie = new ArrayList<>();
			for( Family f : gc.getFamilies() ) {
				for( Media med : f.getAllMedia( gc ) )
					if( med.equals( m ) )
						listaFamiglie.add( f );
			}
			if( !listaPersone.isEmpty() || !listaFonti.isEmpty() || !listaFamiglie.isEmpty() ) {
				LayoutInflater.from(this).inflate( R.layout.titoletto, box, true );
				((TextView)findViewById(R.id.titolo_testo)).setText( R.string.used_by );
			}
			for( Person p : listaPersone )
				U.linkaPersona( box, p, 0 );
			for( Source s : listaFonti )
				U.linkaFonte( box, s );
			for( Family f : listaFamiglie )
				Chiesa.mettiFamiglia( box, f );
		}
	}

	void immaginona( final Media m ) {
		final View vistaMedia = LayoutInflater.from(this).inflate( R.layout.immagine_immagine, box, false );
		box.addView( vistaMedia );
		final ImageView vistaImg = vistaMedia.findViewById( R.id.immagine_foto );
		U.dipingiMedia( m, vistaImg, (ProgressBar)vistaMedia.findViewById(R.id.immagine_circolo) );
		vistaMedia.setOnClickListener( new View.OnClickListener() {
			public void onClick( View vista ) {
				String percorso = (String) vistaImg.getTag( R.id.tag_percorso );
				int tipoFile = (int)vistaImg.getTag(R.id.tag_tipo_file);
				if( tipoFile == 0 ) {	// Il file è da trovare
					U.appAcquisizioneImmagine( Immagine.this, null, 5173, null );
				} else if( tipoFile == 2 || tipoFile == 3 ) { // Apri file con altra app
					// Non male anche se la lista di app generiche fa schifo
					File file = new File( percorso );
					MimeTypeMap mappa = MimeTypeMap.getSingleton();
					String estensione = MimeTypeMap.getFileExtensionFromUrl( file.getName() );
					String tipo = mappa.getMimeTypeFromExtension( estensione.toLowerCase() ); // L'estensione deve essere tutta minuscola
						// Di parecchie estensioni come .pic restituisce null
					Intent intento = new Intent( Intent.ACTION_VIEW );
					Uri uri = Uri.fromFile(file);
					intento.setDataAndType( uri, tipo );
					//s.l( "<" + estensione + "> \"" + tipo + "\"  " + percorso );
					//intento.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );	inutile?
					List<ResolveInfo> resolvers = getPackageManager().queryIntentActivities( intento, 0 );
						// per un'estensione come .tex di cui ha trovato il tipo mime, non c'è nessuna app predefinita
					if( tipo == null || resolvers.isEmpty() ) {
						//Toast.makeText( getApplicationContext(), "Nessuna app trovata per aprire " + file.getName(), Toast.LENGTH_LONG ).show();
						//intento.setType( "*/*" ); poi il file non giunge alla app
						intento.setDataAndType( uri, "*/*" );	// La lista di app fa un po' cagare ma vabè
					}
					// Da android 7 (Nougat api 24) gli uri file:// sono banditi in favore di uri content:// perciò non riesce ad aprire i file
					//intento.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION ); // proposta com soluzione ma nell'emulatore non funziona
					if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) { // ok funziona nell'emulatore con Android 9
						try {
							StrictMode.class.getMethod("disableDeathOnFileUriExposure" ).invoke(null);
						} catch( Exception e ) {
							e.printStackTrace();
						}
					}
					startActivity( intento );


					/*	Un altro modo per avere lo stesso risultato
					try {
						File file = new File( percorso );
						Intent intento = new Intent(Intent.ACTION_VIEW);
						String tipo = URLConnection.guessContentTypeFromStream( new FileInputStream(file) );
						if( tipo == null)
							tipo = URLConnection.guessContentTypeFromName( file.getName() );
						intento.setDataAndType( Uri.fromFile(file), tipo );
						s.l( "<<<" + file.getName() + ">>> \"" + tipo + "\"  " + percorso );
						startActivity( intento );
					} catch( IOException e ) {
						e.printStackTrace();
					}*/

					// Lista migliore delle app installate
					/*File file = new File( percorso );
					MimeTypeMap mappa = MimeTypeMap.getSingleton();
					String estensione = MimeTypeMap.getFileExtensionFromUrl( file.getName() );
					String tipo = mappa.getMimeTypeFromExtension( estensione.toLowerCase() );
					Intent mainIntent = new Intent( Intent.ACTION_MAIN );	// non trovo altro uso che questi due messi Così
					mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
					//mainIntent.setDataAndType( Uri.fromFile(file), tipo );	// no l'INTENTO diventa NON GESTIBILE
					startActivity( mainIntent );

					// Tentativo di avviare il chooser che però non parte
					File file = new File( percorso );
					MimeTypeMap mappa = MimeTypeMap.getSingleton();
					String estensione = MimeTypeMap.getFileExtensionFromUrl( file.getName() );
					String tipo = mappa.getMimeTypeFromExtension( estensione.toLowerCase() );
					Intent intento = new Intent( Intent.ACTION_VIEW );
					intento.setDataAndType( Uri.fromFile(file), tipo );
					intento.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
					Intent chooser = Intent.createChooser( intento, "Open with" );
					if( intento.resolveActivity( getPackageManager() ) != null)
						startActivity(chooser);
					else
						Toast.makeText( getApplicationContext(), "Nessuna app trovata per aprire.",
								Toast.LENGTH_LONG ).show();*/

				} else {
					Intent intento = new Intent( Immagine.this, Lavagna.class );
					intento.putExtra( "percorso", percorso );
					startActivity( intento );
				}
			}
		});
		vistaMedia.setTag( R.id.tag_oggetto, 43614 );	// per il suo menu contestuale
		registerForContextMenu( vistaMedia );
	}

	@Override
	public void elimina() {
		Galleria.eliminaMedia( m, contenitore, null );
	}
}