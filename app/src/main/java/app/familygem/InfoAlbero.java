package app.familygem;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.model.Header;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.parser.JsonParser;
import java.io.File;
import java.io.IOException;
import app.familygem.visita.ListaMedia;

public class InfoAlbero extends AppCompatActivity {

	Gedcom gc;

	@Override
	protected void onCreate( Bundle stato ) {
		super.onCreate( stato );
		setContentView(R.layout.info_albero );
		LinearLayout scatola = findViewById( R.id.info_scatola );

		final int idAlbero = getIntent().getIntExtra( "idAlbero", 1 );
		final Armadio.Cassetto questoAlbero = Globale.preferenze.getAlbero( idAlbero );
		setTitle( questoAlbero.nome );
		final File file = new File( getFilesDir(), idAlbero + ".json");
		String i = getText(R.string.file) + ": " + file.getAbsolutePath();
		if( Globale.gc != null && Globale.preferenze.idAprendo == idAlbero )
			gc = Globale.gc;
		else {
			String contenuto = "";
			try {
				contenuto = FileUtils.readFileToString( file );
			} catch( IOException e ) {
				Toast.makeText( this, e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
			}
			JsonParser jp = new JsonParser();
			gc = jp.fromJson( contenuto );
		}
		if( gc == null )
			i += "\n\n" + getString(R.string.no_useful_data);
		else {
			if( questoAlbero.individui < 100 ) {
				findViewById( R.id.info_aggiorna ).setVisibility( View.GONE );
				aggiornaDati( gc, questoAlbero );
			}
			i += "\n" + getText(R.string.Persons) + ": "+ questoAlbero.individui
				+ "\n" + getText(R.string.families) + ": "+ gc.getFamilies().size()
				+ "\n" + getText(R.string.Generations) + ": "+ questoAlbero.generazioni
				+ "\n" + getText(R.string.media) + ": "+ questoAlbero.media
				+ "\n" + getText(R.string.sources) + ": "+ gc.getSources().size()
				+ "\n" + getText(R.string.repositories) + ": "+ gc.getRepositories().size();
			if( questoAlbero.radice != null ) {
				i += "\n" + "Radice: " + U.epiteto( gc.getPerson(questoAlbero.radice) );
			}
			if( questoAlbero.cartelle != null && !questoAlbero.cartelle.isEmpty() ) {
				i += "\n\n" + "Media folders:";
				for( String dir : questoAlbero.cartelle )
					i += "\n" + dir;
			}
		}
		((TextView)findViewById(R.id.info_statistiche)).setText( i );

		// Bottone "Aggiorna Dati"
		findViewById( R.id.info_aggiorna ).setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				aggiornaDati( gc, questoAlbero);
				recreate();
			}
		});

		if( gc != null ) {
			Header h = gc.getHeader();
			if( h == null) {
				Button bottoneCrea = scatola.findViewById( R.id.info_crea_testata );
				bottoneCrea.setVisibility( View.VISIBLE );
				bottoneCrea.setOnClickListener( new View.OnClickListener() {
					@Override
					public void onClick( View view ) {
						gc.setHeader( AlberoNuovo.creaTestata( file.getName(), false ) );
						U.salvaJson( gc, idAlbero );
						recreate();
					}
				});
			} else {
				scatola.findViewById( R.id.info_testata ).setVisibility( View.VISIBLE );
				if( h.getFile() != null )
					poni( getText(R.string.file),  h.getFile() );
				if( h.getCharacterSet() != null ) {
					poni( getText(R.string.characrter_set), h.getCharacterSet().getValue() );
					poni( getText(R.string.version), h.getCharacterSet().getVersion() );
				}
				spazio();   // uno spazietto
				poni( getText(R.string.language), h.getLanguage() );
				spazio();
				poni( getText(R.string.copyright), h.getCopyright() );
				spazio();
				if (h.getGenerator() != null) {
					poni( getText(R.string.software), h.getGenerator().getName() );
					poni( getText(R.string.version), h.getGenerator().getVersion() );
					if( h.getGenerator().getGeneratorCorporation() != null ) {
						poni( getText(R.string.corporation), h.getGenerator().getGeneratorCorporation().getValue() );
						if( h.getGenerator().getGeneratorCorporation().getAddress() != null )
							poni( getText(R.string.address), h.getGenerator().getGeneratorCorporation().getAddress().getDisplayValue() ); // non è male
						poni( getText(R.string.telephone), h.getGenerator().getGeneratorCorporation().getPhone() );
						poni( getText(R.string.fax), h.getGenerator().getGeneratorCorporation().getFax() );
					}
					spazio();
					if( h.getGenerator().getGeneratorData() != null ) {
						poni( getText(R.string.data), h.getGenerator().getGeneratorData().getValue() );
						poni( getText(R.string.copyright), h.getGenerator().getGeneratorData().getCopyright() );
						poni( getText(R.string.date), h.getGenerator().getGeneratorData().getDate() );
					}
				}
				spazio();
				if( h.getSubmitter(gc) != null )
					poni( getText(R.string.submitter), h.getSubmitter(gc).getName() );	// todo: renderlo cliccabile?
				if( gc.getSubmission() != null )
					poni( getText(R.string.submission), gc.getSubmission().getDescription() );	// todo: cliccabile
				spazio();
				if( h.getGedcomVersion() != null ) {
					poni( getText(R.string.gedcom), h.getGedcomVersion().getVersion() );
					poni( getText(R.string.form), h.getGedcomVersion().getForm() );
				}
				poni( getText(R.string.destination), h.getDestination() );
				spazio();
				if( h.getDateTime() != null ) {
					poni( getText(R.string.date), h.getDateTime().getValue() );
					poni( getText(R.string.time), h.getDateTime().getTime() );
				}
				spazio();
				for( Estensione est : U.trovaEstensioni(h) ) {	// ogni estensione nella sua riga
					poni( est.nome, est.testo );
				}
				spazio();
				// todo rimuovi l'ultimo spazio()

				U.mettiNote( scatola, h, true );
			}
		}
	}

	static void aggiornaDati( Gedcom gc, Armadio.Cassetto albero ) {
		albero.individui = gc.getPeople().size();
		albero.generazioni = quanteGenerazioni( gc, albero.radice );
		ListaMedia visitaMedia = new ListaMedia( gc, true );
		gc.accept( visitaMedia );
		albero.media = visitaMedia.listaMedia.size();
		Globale.preferenze.salva();
		Globale.editato = true; // per aggiornare Alberi quando torna indietro
	}

	boolean testoMesso;  // impedisce di mettere più di uno spazio() consecutivo
	void poni( CharSequence titolo, String testo ) {
		if( testo != null ) {
			TableRow riga = new TableRow( getApplicationContext() );
			TextView cella1 = new TextView( getApplicationContext() );
			cella1.setTextColor( Color.BLACK );
			cella1.setTypeface( null, Typeface.BOLD );
			cella1.setPadding( 0, 0, 10, 0 );
			cella1.setGravity( Gravity.END );
			cella1.setText( titolo );
			riga.addView( cella1 );
			TextView cella2 = new TextView( getApplicationContext() );
			cella2.setTextColor( 0xFF000000 );
			cella2.setPadding( 0, 0, 0, 0 );
			cella2.setText( testo );
			riga.addView( cella2 );
			( (TableLayout) findViewById( R.id.info_tabella ) ).addView( riga );
			testoMesso = true;
		}
	}

	void spazio(){
		if( testoMesso ) {
			TableRow riga = new TableRow( getApplicationContext() );
			View cella = new View( getApplicationContext() );
			cella.setBackgroundResource( R.color.primario );
			riga.addView( cella );
			TableRow.LayoutParams param = (TableRow.LayoutParams) cella.getLayoutParams();
			param.weight = 1;
			param.span = 2;
			param.height = 1;
			param.topMargin = 5;
			param.bottomMargin = 5;
			cella.setLayoutParams( param );
			( (TableLayout) findViewById( R.id.info_tabella ) ).addView( riga );
			testoMesso = false;
		}
	}

	public static int quanteGenerazioni( Gedcom gc, String radice ) {
		if( gc.getPeople().isEmpty() )
			return 0;
		genMin = 0;
		genMax = 0;
		risaliGenerazioni( gc.getPerson(radice), gc, 0 );
		// Rimuove dalle persone l'estensione 'gen' per permettere successivi conteggi
		for( Person p : gc.getPeople() ) {
			p.getExtensions().remove("gen");
			if( p.getExtensions().isEmpty() )
				p.setExtensions( null );
		}
		return 1 - genMin + genMax;
	}

	static int genMin;
	static int genMax;

	// riceve una Person e trova il numero della generazione di antenati più remota
	static void risaliGenerazioni( Person p, Gedcom gc, int gen ) {
		if( gen < genMin )
			genMin = gen;
		// aggiunge l'estensione per indicare che è passato da questa Persona
		p.putExtension( "gen", gen );
		// se è un capostipite va a contare le generazioni di discendenti
		if( p.getParentFamilies(gc).isEmpty() )
			discendiGenerazioni( p, gc, gen );
		for( Family f : p.getParentFamilies(gc) ) {
			// intercetta eventuali fratelli del capostipite
			if( f.getHusbands(gc).isEmpty() && f.getWives(gc).isEmpty() ) {
				for( Person frate : f.getChildren(gc) )
					if( frate.getExtension("gen") == null )
						discendiGenerazioni( frate, gc, gen );
			}
			for( Person padre : f.getHusbands(gc) )
				if( padre.getExtension("gen") == null )
					risaliGenerazioni( padre, gc, gen-1 );
			for( Person madre : f.getWives(gc) )
				if( madre.getExtension("gen") == null )
					risaliGenerazioni( madre, gc, gen-1 );
		}
	}

	// riceve una Person e trova il numero della generazione più remota di discendenti
	static void discendiGenerazioni( Person p, Gedcom gc, int gen ) {
		if( gen > genMax )
			genMax = gen;
		p.putExtension( "gen", gen );
		for( Family fam : p.getSpouseFamilies(gc) ) {
			// individua anche la famiglia dei coniugi
			for( Person moglie : fam.getWives(gc) )
				if( moglie.getExtension("gen") == null )
					risaliGenerazioni( moglie, gc, gen );
			for( Person marito : fam.getHusbands(gc) )
				if( marito.getExtension("gen") == null )
					risaliGenerazioni( marito, gc, gen );
			for( Person figlio : fam.getChildren(gc) )
				discendiGenerazioni( figlio, gc, gen+1 );
		}
	}

	// freccia indietro nella toolbar come quella hardware
	@Override
	public boolean onOptionsItemSelected( MenuItem i ) {
		onBackPressed();
		return true;
	}
}