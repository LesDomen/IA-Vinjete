import React from "react";
import { useForm, SubmitHandler } from "react-hook-form";
import axios from 'axios';

type Inputs = {
    registrska_stevilka: string,
    trajanje: number

};


const FormaZaNakup = (nastavi : (vhod : string) => void) => {
    const { register, handleSubmit, formState: { errors } } = useForm<Inputs>();
    const onSubmit: SubmitHandler<Inputs> = data => {
        console.log(data)
        axios.get(`nakup/nakup?tablica=${data.registrska_stevilka}&trajanje=${data.trajanje}`).then((response) => nastavi(response.data)).catch()
    };

    return (
        /* "handleSubmit" will validate your inputs before invoking "onSubmit" */
        <form onSubmit={handleSubmit(onSubmit)}>
            {/* register your input into the hook by invoking the "register" function */}
            <input defaultValue="MBUR535" {...register("registrska_stevilka", { required: true })} />
            <input defaultValue="240" {...register("trajanje", { required: true })} />


            {errors.registrska_stevilka && <span>This field is required</span>}
            {errors.trajanje && <span>This field is required</span>}


            <input type="submit" />
        </form>
    );
};
export default FormaZaNakup
