import React, {ChangeEvent, useEffect} from 'react';
import './App.css';
import {grpc} from "@improbable-eng/grpc-web";
import {AuthenticationAPI} from './grpc/Authentication_pb_service'
import {Empty} from './grpc/Common_pb'
import { v4 as uuidv4 } from 'uuid';
import * as Authentication_pb from "./grpc/Authentication_pb";
import {Token} from "./grpc/Authentication_pb";
import { useState } from 'react';
import jwt_decode from "jwt-decode";
import axios from 'axios';
import {SubmitHandler, useForm} from "react-hook-form";

type Inputs = {
    registrska_stevilka: string,
    trajanje: string
};

function App() {

    const { register, handleSubmit, formState: { errors } } = useForm<Inputs>();
    const [nakupZaPoslat, setNakupZaPoslat] = useState('');
    const onSubmit: SubmitHandler<Inputs> = data => {
        console.log(data)
        axios.get(`/nakup/nakup?tablica=${data.registrska_stevilka}&trajanje=${data.trajanje}`).then((response) => setNakupZaPoslat(response.data)).catch()
    };

    interface VinjeteTest {
        response: string;
    }
    const defaultService:VinjeteTest[] = [];

    React.useEffect(() => {
        axios
            .get<VinjeteTest[]>("/vinjete/vinjete/test")
            .then(response => {
                console.log(response.data);
                setStatusVinjete(response.data);
                setLoading(false);
            })
            .catch(ex => {
                const error =
                    ex.response.status === 404
                        ? "Resource Not found"
                        : "An unexpected error has occurred";
                setError(error);
                setLoading(false);
            });
    }, []);

    const [statusVinjete, setStatusVinjete]: [VinjeteTest[], (posts: VinjeteTest[]) => void] = React.useState(defaultService);
    const [loading, setLoading]: [boolean, (loading: boolean) => void] = React.useState<boolean>(true);
    const [error, setError]: [string, (error: string) => void] = React.useState("");

    const [registrskaToCheck, setRegistrskaToCheck] = useState('');
    const [aliSmoPreverli, setAliSmoPreverli] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const [preverjenaVinjeta, setPreverjenaVinjeta] = useState('');


    const changeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        setRegistrskaToCheck(event.currentTarget.value);
    }
    const changeHandlerUsername = (event: ChangeEvent<HTMLInputElement>) => {
        setUsername(event.currentTarget.value);
    }
    const changeHandlerPassword = (event: ChangeEvent<HTMLInputElement>) => {
        setPassword(event.currentTarget.value);
    }


    const [token_login, setToken_login] = useState<boolean>(false);

    function login(message: boolean) {

        if (message){
            console.log("Nastavljam na true")
            setToken_login(true);
        }else{
            console.log("Nastavljam na false")
            setToken_login(false);
        }
    }

    // @ts-ignore
    // @ts-ignore
    // @ts-ignore
    // @ts-ignore
    return (
        <div className="App">
            <header className="App-header">
                <button onClick={
                    () => {
                        const meta = new grpc.Metadata();
                        meta.set("correlationId", uuidv4());
                        console.log("Klicali ste Ping   ");
                        grpc.invoke(AuthenticationAPI.Ping, {
                            request: new Empty(),
                            metadata: meta,
                            host: '/auth',
                            onHeaders: (headers: grpc.Metadata) => {
                                console.log(headers);
                            },
                            onMessage: (message: Empty) => {
                                console.log(message);
                            },
                            onEnd: (code: grpc.Code, msg: string, trailers: grpc.Metadata) => {
                                console.log(code, msg, trailers);
                            }
                        });
                    }
                }>
                    Ping
                </button>
                <input
                    placeholder="Vnesi uporabniško ime"
                    id="username"
                    type="text"
                    name="username"
                    className="yellow-input"
                    onChange={changeHandlerUsername}
                />
                <input
                    placeholder="Vnesite geslo"
                    id="password"
                    type="password"
                    name="password"
                    className="yellow-input"
                    onChange={changeHandlerPassword}
                />
                <button onClick={
                    () => {
                        const meta = new grpc.Metadata();
                        meta.set("correlationId", uuidv4());
                        console.log("Registracija");
                        const registracija = new Authentication_pb.UserLogin();
                        registracija.setEmail(username);
                        registracija.setPassword(password);
                        grpc.invoke(AuthenticationAPI.Register, {
                            request: registracija,
                            metadata: meta,
                            host: '/auth',
                            onHeaders: (headers: grpc.Metadata) => {
                                console.log(headers);
                            },
                            onMessage: (message: Token) => {
                                console.log(message);
                            },
                            onEnd: (code: grpc.Code, msg: string, trailers: grpc.Metadata) => {
                                console.log(code, msg, trailers);
                            }
                        });
                    }
                }>
                    Registracija
                </button>

                <button onClick={
                    () => {
                        const meta = new grpc.Metadata();
                        meta.set("correlationId", uuidv4());
                        console.log("Login");
                        const loginiraj = new Authentication_pb.UserLogin();
                        loginiraj.setEmail(username);
                        loginiraj.setPassword(password);
                        grpc.invoke(AuthenticationAPI.Login, {
                            request: loginiraj,
                            metadata: meta,
                            host: '/auth',
                            onHeaders: (headers: grpc.Metadata) => {
                                console.log(headers);
                            },
                            onMessage: (message: Token) => {
                                console.log(message);
                                console.log("Printam JWT:")
                                console.log(message.getToken());

                                let token = message.getToken();
                                let decodedToken = jwt_decode(token);
                                console.log("Decoded Token", decodedToken);
                                let currentDate = new Date();

                                // JWT exp is in seconds
                                // @ts-ignore
                                if (decodedToken.exp * 1000 < currentDate.getTime()) {
                                    console.log("Token expired.");
                                } else {
                                    console.log("Valid token");
                                    login(true);
                                }
                            },
                            onEnd: (code: grpc.Code, msg: string, trailers: grpc.Metadata) => {
                                console.log(code, msg, trailers);
                            }

                        });
                    }
                }>
                    Login
                </button>

                {token_login && <div className="row">
                    <div className="col s6 offset-s3">
                        <h1>Vinjete</h1>
                        <div className="card blue darken-4">
                            <div className="card-content white-text">
                                <span className="card-title"><h2>Preveri vinjete</h2></span>
                                <div className="input-field">
                                    <input
                                        placeholder="Vnesi registrsko številko"
                                        id="vinjeta_registrska"
                                        type="text"
                                        name="registrska_stevilka"
                                        className="yellow-input"
                                        onChange={changeHandler}
                                    />
                                </div>
                            </div>
                            <div className="card-action">
                                <button onClick={() => {
                                    axios.get("veljavnost/history/"+registrskaToCheck).then((response) => {
                                        setPreverjenaVinjeta(response.data);
                                    }).catch();

                                }} className="btn grey lighten-1 black-text">
                                    Potrdi
                                </button>
                                <p>{preverjenaVinjeta}</p>
                            </div>
                        </div>
                    </div>
                    <div className="card blue darken-4">
                        <div className="card-content white-text">
                            <span className="card-title"><h2>Kupi vinjete</h2></span>
                            {/* @ts-ignore*/ }
                            { token_login &&         <form onSubmit={handleSubmit(onSubmit)}>
                                {/* register your input into the hook by invoking the "register" function */}
                                <input defaultValue="MBUR535" {...register("registrska_stevilka", { required: true })} />
                                <input defaultValue="240" {...register("trajanje", { required: true })} />


                                {errors.registrska_stevilka && <span>This field is required</span>}
                                {errors.trajanje && <span>This field is required</span>}


                                <input type="submit" />
                            </form>}
                            <p> {nakupZaPoslat}</p>
                        </div>
                    </div>
                </div>}

                {token_login &&
                    <div>Status service:
                        <>{token_login &&
                            "\n" + statusVinjete}</>
                    </div>
                }

            </header>
        </div>
    );
}

export default App;
