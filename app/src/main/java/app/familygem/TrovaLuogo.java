// Classe di servizio per suggerire i nomi dei luoghi formattati in stile Gedcom

package app.familygem;

import android.content.Context;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.widget.Filter;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;

public class TrovaLuogo extends AppCompatAutoCompleteTextView {

	AdattatoreLista<String> listaLuoghi;
	Task<AutocompletePredictionBufferResponse> resultati;

	public TrovaLuogo( Context contesto, AttributeSet as ) {
		super( contesto, as );
		listaLuoghi = new AdattatoreLista<>( contesto, android.R.layout.simple_spinner_dropdown_item );
		listaLuoghi.setNotifyOnChange(true);
		setAdapter(listaLuoghi);

		final GeoDataClient datiGeo = Places.getGeoDataClient( contesto );
		final AutocompleteFilter filtro = new AutocompleteFilter.Builder().setTypeFilter( AutocompleteFilter.TYPE_FILTER_REGIONS ).build();

		addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged( CharSequence testo, int i, int i1, int i2 ) {}
			@Override
			public void onTextChanged( CharSequence testo, int i, int i1, int i2 ) {}
			@Override
			public void afterTextChanged( Editable editable ) {
				resultati = datiGeo.getAutocompletePredictions( editable.toString(), null, filtro );
				resultati.addOnCompleteListener( ascoltatoreCompletezza );
			}
		} );
	}

	OnCompleteListener<AutocompletePredictionBufferResponse> ascoltatoreCompletezza = new OnCompleteListener<AutocompletePredictionBufferResponse>() {
		@Override
		public void onComplete( Task<AutocompletePredictionBufferResponse > resultati ) {
			if( resultati.isSuccessful() ){
				//AutocompletePredictionBufferResponse predizioni = resultati.getResult();
				ArrayList<AutocompletePrediction> listaPredizioni = DataBufferUtils.freezeAndClose( resultati.getResult() );
				listaLuoghi.clear();
				for( AutocompletePrediction previsto : listaPredizioni ) {
					CharSequence cs = previsto.getFullText( new CharacterStyle() {
						@Override
						public void updateDrawState( TextPaint tp ) {}
					});
					listaLuoghi.add( cs.toString() );
				}
			}
		}
	};

	class AdattatoreLista<T> extends ArrayAdapter<T> {
		AdattatoreLista( Context contesto, int pezzo ) {
			super( contesto, pezzo );
		}
		@Override
		public Filter getFilter() {
			return new Filter() {
				@Override
				protected FilterResults performFiltering( CharSequence cs ) {
					return null;
				}
				@Override
				protected void publishResults( CharSequence cs, FilterResults fr ) {
				}
			};
		}
	}
}
