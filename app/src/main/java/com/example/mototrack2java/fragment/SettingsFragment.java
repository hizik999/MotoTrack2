package com.example.mototrack2java.fragment;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;


import com.example.mototrack2java.MainActivity;
import com.example.mototrack2java.R;
import com.example.mototrack2java.domain.Moto;
import com.example.mototrack2java.rest.MotoApiVolley;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;


public class SettingsFragment extends Fragment {

    private ThemedButton btnCar, btnMoto, btnVoiceOn, btnVoiceOff, btnNotificationOn, btnNotificationOff;
    private Context context;
    private AppCompatButton btn_startTrip;
    private MotoApiVolley motoApiVolley;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        motoApiVolley = new MotoApiVolley(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());


        btnCar = requireView().findViewById(R.id.btnCar);
        btnMoto = requireView().findViewById(R.id.btnMoto);
        btnVoiceOn = requireView().findViewById(R.id.btnVoiceOn);
        btnVoiceOff = requireView().findViewById(R.id.btnVoiceOff);
        btnNotificationOn = requireView().findViewById(R.id.btnNotificationOn);
        btnNotificationOff = requireView().findViewById(R.id.btnNotificationOff);

        btn_startTrip = requireView().findViewById(R.id.btn_startTrip);


        if (((MainActivity) context).loadDataBoolean("status")) {
            btn_startTrip.setText(R.string.cancelTrip);
        }


        btnCar.setOnClickListener(view1 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }

        });

        btnMoto.setOnClickListener(view12 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }
        });

        btnVoiceOn.setOnClickListener(view13 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }
        });

        btnVoiceOff.setOnClickListener(view14 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }
        });

        btnNotificationOn.setOnClickListener(view15 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }
        });

        btnNotificationOff.setOnClickListener(view16 -> {
            if (!((MainActivity) context).loadDataBoolean("status")) {
                btn_startTrip.setText(getText(R.string.startTripSuccess));
            }
        });

        btn_startTrip.setOnClickListener(view17 -> {

            ((MainActivity) context).cancelNotification();

            if (((MainActivity) context).loadDataBoolean("status")) {
                try {
                    motoApiVolley.deleteMoto(((MainActivity) context).loadDataLong("id"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                ((MainActivity) context).saveDataBoolean("status", false);
                btn_startTrip.setText(getText(R.string.startTripSuccess));
//                ((MainActivity) context).cancelNotification(notificationManager, 1);

                ((MainActivity) context).saveDataInt("car_status", 0);
                ((MainActivity) context).cancelTripEditText();
                ((MainActivity) context).saveDataLong("id", -1);

            } else {


                if ((btnCar.isSelected() || btnMoto.isSelected())
                        && (btnVoiceOn.isSelected() || btnVoiceOff.isSelected())
                        && (btnNotificationOn.isSelected() || btnNotificationOff.isSelected())
                        && !((MainActivity) context).loadDataString("loc").equals("")) {

                    if (btnCar.isSelected()) {
                        ((MainActivity) context).saveDataInt("car_status", 0);

                    }

                    if (btnMoto.isSelected()) {
                        ((MainActivity) context).saveDataInt("car_status", 1);


                        if (((MainActivity) context).loadDataLong("id") == -1) {
                            motoApiVolley.addMoto();

                        } else {
                            motoApiVolley.updateMoto(
                                    ((MainActivity) context).loadDataLong("id"),
                                    ((MainActivity) context).loadDataFloat("lat"),
                                    ((MainActivity) context).loadDataFloat("lon")
                            );
                        }
                    }

                    ((MainActivity) context).saveDataBoolean("voiceOn", btnVoiceOn.isSelected());

                    if (btnNotificationOff.isSelected()) {
                        ((MainActivity) context).saveDataBoolean("notification_status", false);
                        ((MainActivity) context).cancelNotification();
                    } else {
                        ((MainActivity) context).saveDataBoolean("notification_status", true);
                        ((MainActivity) context).sendNotificationStatus();
                    }

                    ((MainActivity) context).loadMapFragment();
                    ((MainActivity) context).saveDataBoolean("status", true);
                    ((MainActivity) context).sendNotificationStatus();

                } else {
                    btn_startTrip.setText(getText(R.string.startTripFail));
                    ((MainActivity) context).saveDataBoolean("status", false);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
