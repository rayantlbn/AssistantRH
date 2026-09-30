import { useEffect, useState } from "react";
import axios from "axios";
import { getDashboardJobOffers } from "../services/dashboardApi";
import type { DashboardJobOffer } from "../types/dashboard";

export default function DashboardPage() {
  const [data, setData] = useState<DashboardJobOffer[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    getDashboardJobOffers()
      .then((offers) => {
        if (!cancelled) setData(offers);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(describeError(err));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) return <p>Erreur : {error}</p>;
  if (data === null) return <p>Chargement…</p>;
  return <pre>{JSON.stringify(data, null, 2)}</pre>;
}

function describeError(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response
      ? `HTTP ${err.response.status} ${JSON.stringify(err.response.data)}`
      : `${err.message} (API injoignable ou requête bloquée par CORS)`;
  }
  return String(err);
}
